package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

/**
 * 用户 Mapper 接口
 * 提供用户表的基础CRUD及自定义查询
 *
 * @author poker-platform
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {

    /**
     * 根据用户名查询用户（用于登录校验）
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Select("SELECT * FROM `user` WHERE username = #{username} AND del_flag = 0")
    UserEntity selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户
     *
     * @param mobile 手机号
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE mobile = #{mobile} AND del_flag = 0")
    UserEntity selectByMobile(@Param("mobile") String mobile);

    /**
     * 根据邮箱查询用户
     *
     * @param email 邮箱
     * @return 用户实体
     */
    @Select("SELECT * FROM user WHERE email = #{email} AND del_flag = 0")
    UserEntity selectByEmail(@Param("email") String email);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回 true，否则返回 false
     */
    @Select("SELECT COUNT(1) FROM `user` WHERE username = #{username} AND del_flag = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 统计邮箱是否存在
     *
     * @param email 邮箱
     * @return 数量
     */
    @Select("SELECT COUNT(1) FROM user WHERE email = #{email} AND del_flag = 0")
    int countByEmail(@Param("email") String email);

    /**
     * 统计手机号是否存在
     *
     * @param mobile 手机号
     * @return 数量
     */
    @Select("SELECT COUNT(1) FROM user WHERE mobile = #{mobile} AND del_flag = 0")
    int countByMobile(@Param("mobile") String mobile);

    /**
     * 更新用户的最后登录信息（包括IP、设备、GPS）
     *
     * @param userId       用户ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp   最后登录IP
     * @param deviceId      最后登录设备ID
     * @param latitude      纬度
     * @param longitude     经度
     * @return 影响行数
     */
    @Update("UPDATE user SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp}, " +
            "last_login_device_id = #{deviceId}, last_login_latitude = #{latitude}, last_login_longitude = #{longitude} " +
            "WHERE id = #{userId}")
    int updateLastLoginInfo(@Param("userId") Long userId,
                            @Param("lastLoginTime") LocalDateTime lastLoginTime,
                            @Param("lastLoginIp") String lastLoginIp,
                            @Param("deviceId") String deviceId,
                            @Param("latitude") BigDecimal latitude,
                            @Param("longitude") BigDecimal longitude);

    /**
     * 批量查询用户ID列表（用于批量获取用户信息）
     * 复杂查询写在 XML 中
     *
     * @param userIds 用户ID列表
     * @return 用户实体列表
     */
    List<UserEntity> selectBatchByIds(@Param("userIds") List<Long> userIds);
}
