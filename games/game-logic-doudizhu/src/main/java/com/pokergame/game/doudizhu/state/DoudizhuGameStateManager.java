package com.pokergame.game.doudizhu.state;

import com.pokergame.core.base.BaseGameStateManager;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import lombok.extern.slf4j.Slf4j;

/**
 * 斗地主游戏状态管理器
 *
 * 继承 BaseGameState，只需定义斗地主特有的游戏状态枚举
 * 所有游戏逻辑数据（回合、出牌记录、地主、倍率）都存储在此类中，
 * 斗地主房间（DoudizhuRoom）通过组合方式持有此对象，并委托相关方法。
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuGameStateManager extends BaseGameStateManager<DoudizhuPlayer> {

    /** 当前游戏状态 */
    private DoudizhuGameStatus gameStatus = DoudizhuGameStatus.WAITING;

    /**
     * 构造函数
     *
     * @param roomId     房间ID
     * @param ownerId    房主ID
     * @param maxPlayers 最大玩家数
     */
    public DoudizhuGameStateManager(long roomId, long ownerId, int maxPlayers) {
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

    /**
     * 重置游戏状态（新的一局）
     */
    @Override
    public void reset() {
        this.gameStatus = DoudizhuGameStatus.WAITING;
        // 清空回合数据
        this.getPlayOrder().clear();
        this.setCurrentTurnIndex(0);
        // 清空牌局数据
        this.setLandlordId(0);
        this.getLandlordExtraCards().clear();
        this.setLastPlayCards(null);
        this.setLastPlayPlayerId(0);
        this.setLastPattern(null);
        // 清空倍率
        this.setBombCount(0);
        this.setMultiplier(1);
        for (DoudizhuPlayer player : players.values()) {
            player.reset();
        }
    }
}
