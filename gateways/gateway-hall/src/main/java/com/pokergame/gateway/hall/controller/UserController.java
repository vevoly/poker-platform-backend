package com.pokergame.gateway.hall.controller;

import com.pokergame.common.cmd.AuthCmd;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.constants.MetadataKeys;
import com.pokergame.common.exception.result.Result;
import com.pokergame.common.model.auth.LoginReq;
import com.pokergame.common.model.auth.LoginResp;
import com.pokergame.common.model.auth.LogoutReq;
import com.pokergame.common.model.auth.LogoutResp;
import com.pokergame.common.model.user.*;
import com.pokergame.common.util.ValidationUtils;
import com.pokergame.gateway.hall.constants.ApiPath;
import com.pokergame.gateway.hall.dto.PersonalCenterResp;
import com.pokergame.gateway.hall.service.AggregateService;
import com.pokergame.gateway.hall.service.RouteService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPath.USER)
public class UserController {

    private final RouteService routeService;
    private final AggregateService aggregateService;

    /**
     * 用户注册（无需 Token）
     */
    @PostMapping(ApiPath.USER_REGISTER)
    public Result<RegisterResp> register(@RequestBody RegisterReq req) {
        log.info("注册请求: username={}", req.getUsername());
        ValidationUtils.validate(req);
        return routeService.forwardForResult(UserCmd.CMD, UserCmd.REGISTER, req, RegisterResp.class);
    }

    /**
     * 用户登录（无需 Token）
     */
    @PostMapping(ApiPath.USER_LOGIN)
    public Result<LoginResp> login(@RequestBody LoginReq req) {
        log.info("登录请求: username={}", req.getUsername());
        ValidationUtils.validate(req);
        return routeService.forwardForResult(AuthCmd.CMD, AuthCmd.PASSWORD_LOGIN, req, LoginResp.class);
    }

    /**
     * 获取当前用户信息（需要 Token）
     */
    @GetMapping(ApiPath.USER_INFO)
    public Result<GetUserInfoResp> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(MetadataKeys.USER_ID);
        log.info("获取用户信息: userId={}", userId);
        GetUserInfoReq req = new GetUserInfoReq();
        req.setUserId(userId);
        return routeService.forwardForResult(UserCmd.CMD, UserCmd.GET_USER_INFO, req, userId, GetUserInfoResp.class);
    }

    /**
     * 用户登出（需要 Token）
     */
    @PostMapping(ApiPath.USER_LOGOUT)
    public Result<LogoutResp> logout(@RequestBody LogoutReq req) {
        log.info("登出请求");
        ValidationUtils.validate(req);
        return routeService.forwardForResult(AuthCmd.CMD, AuthCmd.LOGOUT, req, LogoutResp.class);
    }

    /**
     * 获取个人中心数据（用户信息 + 货币列表）
     * @param request
     * @return
     */
    @GetMapping("/personal-center")
    public Result<PersonalCenterResp> getPersonalCenter(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute(MetadataKeys.USER_ID);
        PersonalCenterResp data = aggregateService.getPersonalCenter(userId);
        return Result.success(data);
    }
}
