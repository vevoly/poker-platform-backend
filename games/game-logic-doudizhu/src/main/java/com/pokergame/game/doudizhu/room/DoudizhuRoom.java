package com.pokergame.game.doudizhu.room;

import com.iohao.game.widget.light.room.Player;
import com.pokergame.common.card.Card;
import com.pokergame.core.base.BaseRoom;
import com.pokergame.game.doudizhu.bidding.BiddingManager;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.state.DoudizhuGameStateManager;
import com.pokergame.game.doudizhu.turn.DoudizhuTurnManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 斗地主房间，继承 BaseRoom，通过组合 DoudizhuGameState 管理游戏逻辑。
 * 职责：
 * - 房间管理（继承自 BaseRoom）
 * - 持有游戏状态对象和各管理器
 * - 实现 BaseRoom 的抽象方法，同步房间展示状态与游戏逻辑状态
 */
@Slf4j
@Getter
@Setter
public class DoudizhuRoom extends BaseRoom {

    // ==================== 游戏逻辑组件 ====================
    /** 斗地主游戏状态管理器（所有游戏逻辑数据：回合、出牌记录、地主、倍率等） */
    private DoudizhuGameStateManager stateManager;

    /** 回合管理器（控制叫地主和出牌阶段的超时） */
    private DoudizhuTurnManager turnManager;

    /** 叫地主管理器 */
    private BiddingManager biddingManager;

    // ==================== 构造函数 ====================
    public DoudizhuRoom() {
        super();
    }

    /**
     * 初始化游戏状态（必须在房间创建后、添加玩家前调用一次）
     * 此方法创建 gameState、turnManager、biddingManager，并将当前房间内的玩家同步到 gameState。
     */
    public void initGameState() {
        this.stateManager = new DoudizhuGameStateManager(getRoomId(), getOwnerId(), getMaxPlayers());
        // 将当前房间内的玩家同步到 gameState（房间创建时房主已加入）
        for (Player player : getPlayerMap().values()) {
            stateManager.addPlayer((DoudizhuPlayer) player);
        }
        this.turnManager = new DoudizhuTurnManager(this);
        this.biddingManager = new BiddingManager(this);
        // 设置回合管理器的叫地主管理器引用（用于超时回调）
        this.turnManager.setBiddingManager(this.biddingManager);
    }

    // ==================== 实现 BaseRoom 的抽象方法 ====================

    /**
     * 更新游戏状态（同时更新基类的 gameStatus 字符串和 gameState 的枚举状态）
     * @param status 游戏状态枚举（必须为 DoudizhuGameStatus 类型）
     */
    @Override
    public void updateGameStatus(Enum<?> status) {
        DoudizhuGameStatus ds = (DoudizhuGameStatus) status;
        // 更新基类的字符串状态（用于客户端展示）
        super.setGameStatus(ds.name());   // 调用父类 Lombok 生成的 setter
        // 同步更新游戏逻辑状态
        if (stateManager != null) {
            stateManager.changeStatus(ds);
        }
    }

    @Override
    public Enum<?> getGameStatusEnum() {
        return DoudizhuGameStatus.valueOf(getGameStatus());
    }

    public DoudizhuGameStatus getDoudizhuGameStatus() {
        return DoudizhuGameStatus.valueOf(getGameStatus());
    }


    // ==================== 玩家管理增强（同步到 gameState） ====================

    /**
     * 添加玩家（重写基类方法，同步到 gameState）
     * @param player 玩家对象
     */
    @Override
    public void addPlayer(Player player) {
        super.addPlayer(player);
        if (stateManager != null) {
            stateManager.addPlayer((DoudizhuPlayer) player);
        }
    }

    /**
     * 移除玩家（重写基类方法，同步到 gameState）
     * @param player 玩家对象
     */
    @Override
    public void removePlayer(Player player) {
        super.removePlayer(player);
        if (stateManager != null) {
            stateManager.removePlayer(player.getUserId());
        }
    }

    // ==================== 游戏逻辑委托方法（转发给 gameState） ====================

    /** 获取当前回合玩家 */
    public DoudizhuPlayer getCurrentPlayer() {
        return stateManager != null ? stateManager.getCurrentPlayer() : null;
    }

    /** 判断指定玩家是否为当前回合玩家 */
    public boolean isCurrentPlayer(long userId) {
        return stateManager != null && stateManager.isCurrentPlayer(userId);
    }

    /** 切换到下一个玩家 */
    public void nextTurn() {
        if (stateManager != null) stateManager.nextTurn();
    }

    /** 设置出牌顺序 */
    public void setPlayOrder(List<Long> order, long startPlayerId) {
        if (stateManager != null) stateManager.setPlayOrder(order, startPlayerId);
    }

    /** 增加炸弹计数（更新倍率） */
    public void addBomb() {
        if (stateManager != null) stateManager.addBomb();
    }

    /** 获取地主ID */
    public long getLandlordId() {
        return stateManager != null ? stateManager.getLandlordId() : 0;
    }

    /** 设置地主ID */
    public void setLandlordId(long landlordId) {
        if (stateManager != null) stateManager.setLandlordId(landlordId);
    }

    /** 获取当前倍数 */
    public int getCurrentMultiple() {
        return stateManager != null ? stateManager.getMultiplier() : 1;
    }

    /** 获取地主底牌 */
    public List<Card> getLandlordExtraCards() {
        return stateManager != null ? stateManager.getLandlordExtraCards() : new ArrayList<>();
    }

    // ==================== 斗地主特有便捷方法 ====================

    /** 类型安全地获取斗地主玩家 */
    public DoudizhuPlayer getDoudizhuPlayer(long userId) {
        return (DoudizhuPlayer) getPlayerById(userId);
    }

    /** 获取所有斗地主玩家列表 */
    public List<DoudizhuPlayer> getAllDoudizhuPlayers() {
        return getPlayerMap().values().stream()
                .map(p -> (DoudizhuPlayer) p)
                .collect(Collectors.toList());
    }

    // ==================== 重置 ====================

    /**
     * 重置房间（重写基类方法，同时重置 gameState 和管理器）
     */
    @Override
    public void reset() {
        super.reset();
        if (stateManager != null) stateManager.reset();
        if (biddingManager != null) biddingManager.reset();
        if (turnManager != null) turnManager.reset();
        log.info("斗地主房间 {} 已重置", getRoomId());
    }

    @Override
    public String toString() {
        return String.format("DoudizhuRoom{roomId=%d, gameStatus=%s, playerCount=%d, maxPlayers=%d, ownerId=%d}",
                getRoomId(), getGameStatus(), getPlayerCount(), getMaxPlayers(), getOwnerId());
    }
}
