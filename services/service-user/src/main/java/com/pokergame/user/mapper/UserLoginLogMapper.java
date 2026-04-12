package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.UserLoginLogEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户登录日志 Mapper 接口
 * 提供登录日志表的CRUD操作
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Mapper
public interface UserLoginLogMapper extends BaseMapper<UserLoginLogEntity> {
}
