package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Getter;

import java.util.Map;

/**
 * 游戏结束事件
 * 发布时机：游戏分出胜负，即将结算时
 * 机器人服务收到后应清理该房间的状态
 */
@Getter
public class GameOverEvent extends BaseGameEvent {
    /** 获胜玩家ID */
    private final long winnerId;
    /** 最终分数映射（playerId -> 分数） */
    private final Map<Long, Integer> finalScores;

    public GameOverEvent(GameType gameType, String roomId, long winnerId, Map<Long, Integer> finalScores) {
        super(GameEventType.GAME_END, gameType, roomId);
        this.winnerId = winnerId;
        this.finalScores = finalScores;
    }
}
