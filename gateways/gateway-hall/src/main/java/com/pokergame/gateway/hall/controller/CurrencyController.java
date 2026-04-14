package com.pokergame.gateway.hall.controller;

import com.pokergame.common.cmd.CurrencyCmd;
import com.pokergame.common.constants.MetadataKeys;
import com.pokergame.common.exception.result.Result;
import com.pokergame.common.model.user.GetCurrencyReq;
import com.pokergame.common.model.user.GetCurrencyResp;
import com.pokergame.gateway.hall.constants.ApiPath;
import com.pokergame.gateway.hall.service.RouteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(ApiPath.CURRENCY)
@RequiredArgsConstructor
public class CurrencyController {

    private final RouteService routeService;

    /**
     * 获取当前用户的所有货币信息（需要 Token）
     */
    @GetMapping(ApiPath.CURRENCY_LIST)
    public Result<GetCurrencyResp> getCurrency(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(MetadataKeys.USER_ID);
        log.info("查询货币列表: userId={}", userId);
        GetCurrencyReq req = new GetCurrencyReq();
        req.setUserId(userId);
        return routeService.forwardForResult(CurrencyCmd.CMD, CurrencyCmd.GET_CURRENCY, req, userId, GetCurrencyResp.class);
    }
}
