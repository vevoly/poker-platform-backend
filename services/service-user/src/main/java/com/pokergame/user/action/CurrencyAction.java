package com.pokergame.user.action;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.pokergame.common.cmd.CurrencyCmd;
import com.pokergame.common.enums.ChangeCurrencyType;
import com.pokergame.common.enums.CurrencyType;
import com.pokergame.common.model.user.*;
import com.pokergame.common.util.ValidationUtils;
import com.pokergame.user.converter.CurrencyChangeLogConverter;
import com.pokergame.user.converter.CurrencyConverter;
import com.pokergame.user.entity.CurrencyChangeLogEntity;
import com.pokergame.user.entity.UserCurrencyEntity;
import com.pokergame.user.mapper.CurrencyChangeLogMapper;
import com.pokergame.user.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 货币模块 RPC Action
 * 负责货币查询、增加、减少、流水查询等操作
 *
 * @author poker-platform
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ActionController(CurrencyCmd.CMD)
public class CurrencyAction {

    private final CurrencyService currencyService;
    private final CurrencyConverter currencyConverter;
    private final CurrencyChangeLogMapper currencyChangeLogMapper;
    private final CurrencyChangeLogConverter currencyChangeLogConverter;

    /**
     * 查询用户货币
     *
     * @param req 请求（用户ID，可选货币类型）
     * @return 货币列表
     */
    @ActionMethod(CurrencyCmd.GET_CURRENCY)
    public GetCurrencyResp getCurrency(GetCurrencyReq req) {
        log.info("RPC 查询货币: userId={}, currencyType={}", req.getUserId(), req.getCurrencyType());
        ValidationUtils.validate(req);

        List<UserCurrencyEntity> currencies;
        if (req.getCurrencyType() != null && !req.getCurrencyType().isEmpty()) {
            // 查询指定货币
            UserCurrencyEntity entity = currencyService.getUserCurrency(req.getUserId(),
                    CurrencyType.fromCode(req.getCurrencyType()));
            currencies = Collections.singletonList(entity);
        } else {
            // 查询所有货币
            currencies = currencyService.getUserCurrencies(req.getUserId());
        }

        GetCurrencyResp resp = new GetCurrencyResp();
        resp.setCurrencies(currencyConverter.toDTOList(currencies));
        return resp;
    }

    /**
     * 增加货币
     *
     * @param req 增加请求（用户ID，货币类型，数量，变更类型等）
     * @return 操作结果（变更后数量）
     */
    @ActionMethod(CurrencyCmd.INCREASE)
    public IncreaseCurrencyResp increase(IncreaseCurrencyReq req) {
        log.info("RPC 增加货币: userId={}, currencyType={}, amount={}",
                req.getUserId(), req.getCurrencyType(), req.getAmount());
        ValidationUtils.validate(req);

        Long afterAmount = currencyService.increaseCurrency(
                req.getUserId(),
                CurrencyType.fromCode(req.getCurrencyType()),
                req.getAmount(),
                ChangeCurrencyType.fromCode(req.getChangeType()),
                req.getOrderId(),
                req.getRemark()
        );

        IncreaseCurrencyResp resp = new IncreaseCurrencyResp();
        resp.setAfterAmount(afterAmount);
        return resp;
    }

    /**
     * 减少货币
     *
     * @param req 减少请求（用户ID，货币类型，数量，变更类型等）
     * @return 操作结果（变更后数量）
     */
    @ActionMethod(CurrencyCmd.DECREASE)
    public DecreaseCurrencyResp decrease(DecreaseCurrencyReq req) {
        log.info("RPC 减少货币: userId={}, currencyType={}, amount={}",
                req.getUserId(), req.getCurrencyType(), req.getAmount());
        ValidationUtils.validate(req);

        Long afterAmount = currencyService.decreaseCurrency(
                req.getUserId(),
                CurrencyType.fromCode(req.getCurrencyType()),
                req.getAmount(),
                ChangeCurrencyType.fromCode(req.getChangeType()),
                req.getOrderId(),
                req.getRemark()
        );

        DecreaseCurrencyResp resp = new DecreaseCurrencyResp();
        resp.setAfterAmount(afterAmount);
        return resp;
    }

    /**
     * 查询货币变更流水（分页）
     *
     * @param req 请求（用户ID，页码，每页大小）
     * @return 流水列表及分页信息
     */
    @ActionMethod(CurrencyCmd.GET_LOG)
    public GetCurrencyLogResp getLog(GetCurrencyLogReq req) {
        log.info("RPC 查询货币流水: userId={}, page={}, size={}",
                req.getUserId(), req.getPage(), req.getPage());
        ValidationUtils.validate(req);

        // 分页查询
        Page<CurrencyChangeLogEntity> page = new Page<>(req.getPage(), req.getSize());
        LambdaQueryWrapper<CurrencyChangeLogEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CurrencyChangeLogEntity::getUserId, req.getUserId())
                .orderByDesc(CurrencyChangeLogEntity::getCreateTime);
        Page<CurrencyChangeLogEntity> entityPage = currencyChangeLogMapper.selectPage(page, wrapper);

        // 转换 DTO
        List<CurrencyChangeLogDTO> records = currencyChangeLogConverter.toDTOList(entityPage.getRecords());

        // 构建响应
        GetCurrencyLogResp resp = new GetCurrencyLogResp();
        resp.setRecords(records);
        resp.setTotal(entityPage.getTotal());
        resp.setPage(req.getPage());
        resp.setSize(req.getSize());
        return resp;
    }
}
