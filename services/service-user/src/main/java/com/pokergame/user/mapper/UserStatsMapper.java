package com.pokergame.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pokergame.user.entity.UserStatsEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户统计 Mapper 接口
 *
 * @author poker-platform
 */
@Mapper
public interface UserStatsMapper extends BaseMapper<UserStatsEntity> {

    /**
     * 根据用户ID查询统计信息
     *
     * @param userId 用户ID
     * @return 统计实体
     */
    @Select("SELECT * FROM user_stats WHERE user_id = #{userId}")
    UserStatsEntity selectByUserId(@Param("userId") Long userId);

    /**
     * 增加胜利场次（事务中调用）
     *
     * <p>胜利时：胜场+1，连胜+1，连败清零
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_stats SET " +
            "total_games = total_games + 1, " +
            "win_games = win_games + 1, " +
            "consecutive_wins = consecutive_wins + 1, " +
            "consecutive_losses = 0, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementWinGames(@Param("userId") Long userId);

    /**
     * 增加失败场次
     *
     * <p>失败时：总局数+1，连败+1，连胜清零
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_stats SET " +
            "total_games = total_games + 1, " +
            "consecutive_wins = 0, " +
            "consecutive_losses = consecutive_losses + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementLossGames(@Param("userId") Long userId);

    /**
     * 平局处理
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_stats SET " +
            "total_games = total_games + 1, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int incrementDrawGames(@Param("userId") Long userId);

    /**
     * 重置连胜/连败记录（退出房间时可选）
     *
     * @param userId 用户ID
     * @return 影响行数
     */
    @Update("UPDATE user_stats SET " +
            "consecutive_wins = 0, " +
            "consecutive_losses = 0, " +
            "update_time = NOW() " +
            "WHERE user_id = #{userId}")
    int resetConsecutive(@Param("userId") Long userId);
}
