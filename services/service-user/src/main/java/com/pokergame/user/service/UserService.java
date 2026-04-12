package com.pokergame.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.pokergame.common.model.user.LoginReq;
import com.pokergame.common.model.user.RegisterReq;
import com.pokergame.user.entity.UserEntity;

/**
 * 用户服务接口
 *
 * <p>继承 IService 获得 MyBatis-Plus 通用 Service 能力
 *
 * @author poker-platform
 */
public interface UserService extends IService<UserEntity> {

    /**
     * 用户注册
     *
     * @param req 注册请求（包含用户名/手机/邮箱、密码、风控数据等）
     * @return 用户ID
     */
    Long register(RegisterReq req);

    /**
     * 用户登录
     *
     * @param req 登录请求（支持用户名/手机/邮箱，包含密码和风控数据）
     * @return 用户实体
     */
    UserEntity login(LoginReq req);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity getByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户实体
     */
    UserEntity getByMobile(String mobile);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    UserEntity getByEmail(String email);

    /**
     * 校验用户是否存在且状态正常
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    UserEntity checkAndGetUser(Long userId);
}
