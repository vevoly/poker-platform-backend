package com.pokergame.gateway.hall.service;

import com.pokergame.common.cmd.CurrencyCmd;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.model.user.*;
import com.pokergame.gateway.hall.dto.PersonalCenterResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 数据聚合服务
 * 聚合多个 RPC 调用的结果，减少客户端请求次数
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AggregateService {

    private final RouteService routeService;

    /**
     * 获取个人中心数据（用户信息 + 货币列表）
     * 后续可使用 @JMultiCacheable 注解缓存结果
     */
    public PersonalCenterResp getPersonalCenter(Long userId) {
        // 并行调用多个服务
        CompletableFuture<UserDTO> userFuture = CompletableFuture.supplyAsync(() -> {
            GetUserInfoReq req = new GetUserInfoReq();
            req.setUserId(userId);
            GetUserInfoResp resp = routeService.forwardForData(
                    UserCmd.CMD, UserCmd.GET_USER_INFO, req, GetUserInfoResp.class);
            return resp.getUser();
        });

        CompletableFuture<List<UserCurrencyDTO>> currencyFuture = CompletableFuture.supplyAsync(() -> {
            GetCurrencyReq req = new GetCurrencyReq();
            req.setUserId(userId);
            GetCurrencyResp resp = routeService.forwardForData(
                    CurrencyCmd.CMD, CurrencyCmd.GET_CURRENCY, req, GetCurrencyResp.class);
            return resp.getCurrencies();
        });

        // 等待所有结果返回
        CompletableFuture.allOf(userFuture, currencyFuture).join();

        PersonalCenterResp resp = new PersonalCenterResp();
        resp.setUser(userFuture.join());
        resp.setCurrencies(currencyFuture.join());
        return resp;
    }
}
