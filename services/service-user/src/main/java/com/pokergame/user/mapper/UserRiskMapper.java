package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.UserRiskEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户风控 Mapper 接口
 * 提供风控表的CRUD操作
 *
 * @author poker-platform
 * @since 1.0.0
 */
@Mapper
public interface UserRiskMapper extends BaseMapper<UserRiskEntity> {
}
