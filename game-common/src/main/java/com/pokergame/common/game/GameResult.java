package com.pokergame.common.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏结果
 * 用于存储对局结束后的结算信息
 *
 * @author poker-platform
 */
@Data
public class GameResult {

    /** 游戏类型 */
    private GameType gameType;

    /** 房间ID */
    private String roomId;

    /** 赢家ID列表（可能有多个，如德州扑克多人赢钱） */
    private List<Long> winnerIds = new ArrayList<>();

    /** 玩家分数变化 */
    private Map<Long, Long> scoreChanges = new ConcurrentHashMap<>();

    /** 玩家金币变化 */
    private Map<Long, Long> goldChanges = new ConcurrentHashMap<>();

    /** 玩家经验变化 */
    private Map<Long, Integer> expChanges = new ConcurrentHashMap<>();

    /** 对局开始时间 */
    private long startTime;

    /** 对局结束时间 */
    private long endTime;

    /** 对局总时长（秒） */
    private int duration;

    /** 是否提前结束（如有人逃跑） */
    private boolean earlyTermination;

    /** 额外数据（如牌谱、操作记录） */
    private Map<String, Object> extra = new ConcurrentHashMap<>();

    public void addWinner(Long userId) {
        winnerIds.add(userId);
    }

    public void addGoldChange(Long userId, long change) {
        goldChanges.put(userId, change);
    }

    public void addScoreChange(Long userId, long change) {
        scoreChanges.put(userId, change);
    }

    public void addExpChange(Long userId, int change) {
        expChanges.put(userId, change);
    }

    public long getTotalGoldChange(Long userId) {
        return goldChanges.getOrDefault(userId, 0L);
    }

    @Override
    public String toString() {
        return String.format("GameResult{game=%s, winners=%s, changes=%s}",
                gameType, winnerIds, goldChanges);
    }
}
