package com.pokergame.game.doudizhu.state;

import com.pokergame.core.base.BaseGameState;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主游戏状态
 *
 * 继承 BaseGameState，只需定义斗地主特有的游戏状态枚举
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuGameState extends BaseGameState<DoudizhuPlayer> {

    /** 当前游戏状态 */
    private DoudizhuGameStatus gameStatus = DoudizhuGameStatus.WAITING;

    /**
     * 构造函数
     *
     * @param roomId     房间ID
     * @param ownerId    房主ID
     * @param maxPlayers 最大玩家数
     */
    public DoudizhuGameState(long roomId, long ownerId, int maxPlayers) {
        super(roomId, ownerId, maxPlayers);
    }

    @Override
    public void changeStatus(Enum<?> newStatus) {
        this.gameStatus = (DoudizhuGameStatus) newStatus;
        log.info("房间 {} 状态变更: {}", getRoomId(), gameStatus);
    }

    @Override
    public Enum<?> getCurrentStatus() {
        return gameStatus;
    }

    @Override
    public void reset() {
        this.gameStatus = DoudizhuGameStatus.WAITING;
        this.getPlayOrder().clear();
        this.setCurrentTurnIndex(0);
        this.setLandlordId(0);
        this.getLandlordExtraCards().clear();
        this.setLastPlayCards(null);
        this.setLastPlayPlayerId(0);
        this.setLastPattern(null);
        this.setBombCount(0);
        this.setMultiplier(1);

        for (DoudizhuPlayer player : players.values()) {
            player.reset();
        }
    }
}
