package com.pokergame.common.event;

import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;
import lombok.Getter;

import java.util.List;

/**
 * 游戏开始事件
 * 发布时机：游戏初始化完成，即将发牌或进入第一回合前
 */
@Getter
public class GameStartEvent extends BaseGameEvent {
    /** 玩家ID列表（按座位顺序） */
    private final List<Long> playerIds;   // 座位顺序
    /** 初始分数/金币（每个玩家初始值） */
    private final int initScore;           // 初始分数/金币

    public GameStartEvent(GameType gameType, String roomId, List<Long> playerIds, int initScore) {
        super(GameEventType.GAME_START, gameType, roomId);
        this.playerIds = playerIds;
        this.initScore = initScore;
    }
}
