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

    @Override
    public void changeStatus(Enum<?> newStatus) {
        this.gameStatus = (DoudizhuGameStatus) newStatus;
        log.info("房间状态变更: {}", gameStatus);
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
        super.resetRound();
        this.gameStatus = DoudizhuGameStatus.WAITING;
    }
}
