package com.pokergame.user.service;

import com.pokergame.user.entity.UserStatsEntity;

/**
 * 用户统计服务接口
 *
 * @author poker-platform
 */
public interface UserStatsService {

    /**
     * 初始化用户统计信息
     * @param userId
     */
    void initUserStats(Long userId);

    /**
     * 获取用户统计信息
     *
     * @param userId 用户ID
     * @return 统计实体
     */
    UserStatsEntity getUserStats(Long userId);

    /**
     * 记录胜利
     *
     * @param userId 用户ID
     */
    void recordWin(Long userId);

    /**
     * 记录失败
     *
     * @param userId 用户ID
     */
    void recordLoss(Long userId);

    /**
     * 记录平局
     *
     * @param userId 用户ID
     */
    void recordDraw(Long userId);

    /**
     * 重置连胜/连败
     *
     * @param userId 用户ID
     */
    void resetConsecutive(Long userId);
}
