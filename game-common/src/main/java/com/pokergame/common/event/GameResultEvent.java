package com.pokergame.common.event;


import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Getter;

import java.util.Map;

/**
 * 游戏结果事件
 *
 * 用于：游戏结束后通知其他服务
 *
 * @author poker-platform
 */
@Getter
public class GameResultEvent extends BaseGameEvent {

    private final long winnerId;
    private final Map<Long, Long> goldChanges;
    private final Map<Long, Integer> scoreChanges;
    private final int multiplier;
    private final boolean isSpring;
    private final String gameData;  // JSON格式的游戏数据（用于回放）

    public GameResultEvent(GameType gameType, String roomId, long winnerId,
                           Map<Long, Long> goldChanges, int multiplier) {
        super(GameEventType.GAME_END, gameType, roomId);
        this.winnerId = winnerId;
        this.goldChanges = goldChanges;
        this.scoreChanges = null;
        this.multiplier = multiplier;
        this.isSpring = false;
        this.gameData = null;
    }

    public GameResultEvent(GameType gameType, String roomId, long winnerId,
                           Map<Long, Long> goldChanges, Map<Long, Integer> scoreChanges,
                           int multiplier, boolean isSpring, String gameData) {
        super(GameEventType.GAME_END, gameType, roomId);
        this.winnerId = winnerId;
        this.goldChanges = goldChanges;
        this.scoreChanges = scoreChanges;
        this.multiplier = multiplier;
        this.isSpring = isSpring;
        this.gameData = gameData;
    }
}
