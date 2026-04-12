package com.pokergame.user.service.impl;

import com.pokergame.common.exception.GameCode;
import com.pokergame.user.entity.UserStatsEntity;
import com.pokergame.user.mapper.UserStatsMapper;
import com.pokergame.user.service.UserStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户统计服务实现类
 *
 * @author poker-platform
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsServiceImpl implements UserStatsService {

    private final UserStatsMapper userStatsMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initUserStats(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");
        UserStatsEntity stats = new UserStatsEntity();
        stats.setUserId(userId);
        stats.setTotalGames(0);
        stats.setWinGames(0);
        stats.setConsecutiveWins(0);
        stats.setConsecutiveLosses(0);
        userStatsMapper.insert(stats);
        log.debug("用户统计初始化成功，userId={}", userId);
    }

    @Override
    public UserStatsEntity getUserStats(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        UserStatsEntity stats = userStatsMapper.selectByUserId(userId);
        GameCode.USER_STATS_NOT_FOUND.assertTrueThrows(stats == null);

        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordWin(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        int result = userStatsMapper.incrementWinGames(userId);
        if (result <= 0) {
            log.warn("记录胜利失败，userId: {}", userId);
            GameCode.USER_STATS_UPDATE_FAILED.assertTrueThrows(true);
        }
        log.debug("记录胜利成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordLoss(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        int result = userStatsMapper.incrementLossGames(userId);
        if (result <= 0) {
            log.warn("记录失败失败，userId: {}", userId);
            GameCode.USER_STATS_UPDATE_FAILED.assertTrueThrows(true);
        }
        log.debug("记录失败成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordDraw(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        int result = userStatsMapper.incrementDrawGames(userId);
        if (result <= 0) {
            log.warn("记录平局失败，userId: {}", userId);
            GameCode.USER_STATS_UPDATE_FAILED.assertTrueThrows(true);
        }
        log.debug("记录平局成功，userId: {}", userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetConsecutive(Long userId) {
        GameCode.PARAM_ERROR.assertTrueThrows(userId == null, "用户ID不能为空");

        int result = userStatsMapper.resetConsecutive(userId);
        if (result <= 0) {
            log.warn("重置连胜/连败失败，userId: {}", userId);
        }
        log.debug("重置连胜/连败成功，userId: {}", userId);
    }
}