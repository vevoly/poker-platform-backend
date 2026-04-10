package com.pokergame.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
     * @param username 用户名
     * @param password 密码（明文）
     * @param nickname 昵称
     * @return 用户ID
     */
    Long register(String username, String password, String nickname);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @return 用户实体
     */
    UserEntity login(String username, String password);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户实体
     */
    UserEntity getByUsername(String username);

    /**
     * 更新最后登录时间
     *
     * @param userId 用户ID
     */
    void updateLastLoginTime(Long userId);

    /**
     * 校验用户是否存在
     *
     * @param userId 用户ID
     * @return 用户实体
     */
    UserEntity checkAndGetUser(Long userId);
}
