package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.time.LocalDateTime;

/**
 * 用户 Mapper 接口
 *
 * <p>继承 BaseMapper 获得基础 CRUD 能力：
 * <ul>
 *   <li>insert - 插入用户</li>
 *   <li>selectById - 根据ID查询</li>
 *   <li>updateById - 根据ID更新</li>
 *   <li>deleteById - 逻辑删除（自动转换为 update del_flag=1）</li>
 * </ul>
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
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return 存在返回 true，否则返回 false
     */
    @Select("SELECT COUNT(1) FROM `user` WHERE username = #{username} AND del_flag = 0")
    int countByUsername(@Param("username") String username);

    /**
     * 更新最后登录时间
     *
     * @param userId         用户ID
     * @param lastLoginTime  最后登录时间
     * @return 影响行数
     */
    @Update("UPDATE `user` SET last_login_time = #{lastLoginTime}, " +
            "update_time = NOW() " +
            "WHERE id = #{userId} AND del_flag = 0")
    int updateLastLoginTime(@Param("userId") Long userId,
                            @Param("lastLoginTime") LocalDateTime lastLoginTime);

    /**
     * 批量查询用户ID列表（用于批量获取用户信息）
     * 复杂查询写在 XML 中
     *
     * @param userIds 用户ID列表
     * @return 用户实体列表
     */
    List<UserEntity> selectBatchByIds(@Param("userIds") List<Long> userIds);
}
