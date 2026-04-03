package com.pokergame.core.player;

import lombok.Data;

/**
 * 游戏玩家 - 纯数据结构
 *
 * @author poker-platform
 */
@Data
public class GamePlayer {

    /** 玩家ID */
    private final long playerId;

    /** 玩家名称 */
    private final String playerName;

    /** 玩家状态 */
    private PlayerStatus status = PlayerStatus.WAITING;

    /** 是否地主 */
    private boolean isLandlord = false;

    /** 加入时间 */
    private final long joinTime = System.currentTimeMillis();

    public GamePlayer(long playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
    }

    /**
     * 是否为当前回合玩家（由外部设置）
     */
    private transient boolean isCurrentTurn = false;
}
