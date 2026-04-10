package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.CurrencyChangeLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 货币变更流水 Mapper 接口
 *
 * <p>用于审计和对账，只读操作
 *
 * @author poker-platform
 */
@Mapper
public interface CurrencyChangeLogMapper extends BaseMapper<CurrencyChangeLogEntity> {

    /**
     * 查询用户某时间范围内的流水
     *
     * @param userId    用户ID
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return 流水列表
     */
    @Select("SELECT * FROM currency_change_log " +
            "WHERE user_id = #{userId} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY create_time DESC")
    List<CurrencyChangeLogEntity> selectByTimeRange(@Param("userId") Long userId,
                                                    @Param("startTime") LocalDateTime startTime,
                                                    @Param("endTime") LocalDateTime endTime);
}
