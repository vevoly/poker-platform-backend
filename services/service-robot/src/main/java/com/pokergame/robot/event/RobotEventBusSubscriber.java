package com.pokergame.robot.event;

import com.iohao.game.action.skeleton.eventbus.EventBusSubscriber;
import com.iohao.game.action.skeleton.eventbus.EventSubscribe;
import com.iohao.game.action.skeleton.eventbus.ExecutorSelector;
import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.event.*;
import com.pokergame.robot.manager.RobotManager;
import com.pokergame.starter.spring.context.SpringContextHolder;
import lombok.extern.slf4j.Slf4j;

/**
 * 机器人服务事件总线订阅者
 * 使用 ioGame 框架的 @EventBusSubscriber 和 @EventSubscribe 注解
 *
 * 注意：
 * 1. 此类不是 Spring Bean，不会被 Spring 管理
 * 2. 需要手动在 RobotLogicStartup 中注册到 EventBus
 * 3. RobotManager 使用单例模式，通过 getInstance() 获取
 */
@Slf4j
@EventBusSubscriber
public class RobotEventBusSubscriber {

    // 注意：不能使用 @Autowired，这里通过工具类获取
    private static RobotManager getRobotManager() {
        return SpringContextHolder.getBean(RobotManager.class);
    }

    /**
     * 处理游戏开始事件
     * 使用虚拟线程执行器，避免阻塞事件总线
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onGameStart(GameStartEvent event) {
        log.info("收到游戏开始事件: roomId={}, gameType={}, players={}",
                event.getRoomId(), event.getGameType(), event.getPlayerIds());
       getRobotManager().onGameStart(event);
    }

    /**
     * 处理回合切换事件
     * 此事件是触发机器人决策的关键
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onTurnChanged(TurnChangedEvent event) {
        log.info("收到回合切换事件: roomId={}, currentPlayer={}, timeout={}s",
                event.getRoomId(), event.getCurrentPlayerId(), event.getTimeoutSeconds());
        getRobotManager().onTurnChanged(event);
    }

    /**
     * 处理发牌事件
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onCardsDealt(CardsDealtEvent event) {
        log.info("收到发牌事件: roomId={}, playerId={}, cardsCount={}",
                event.getRoomId(), event.getPlayerId(),
                event.getHandCards() != null ? event.getHandCards().size() : 0);
        getRobotManager().onCardsDealt(event);
    }

    /**
     * 处理叫地主/抢地主事件
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onBidding(BiddingEvent event) {
        log.info("收到叫地主事件: roomId={}, playerId={}, multiple={}",
                event.getRoomId(), event.getPlayerId(), event.getMultiple());
        getRobotManager().onBidding(event);
    }

    /**
     * 处理出牌动作事件（仅处理 DOUDIZHU_PLAY_CARD 类型）
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onCardPlayed(GameActionEvent event) {
        // 仅处理出牌类型的事件
        if (event.getEventType() == GameEventType.DOUDIZHU_PLAY_CARD) {
            log.info("收到出牌事件: roomId={}, playerId={}, cards={}, pattern={}",
                    event.getRoomId(), event.getPlayerId(), event.getCards(), event.getPattern());
            getRobotManager().onCardPlayed(event);
        }
    }

    /**
     * 处理过牌事件
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onPass(PassEvent event) {
        log.info("收到过牌事件: roomId={}, playerId={}", event.getRoomId(), event.getPlayerId());
        getRobotManager().onPass(event);
    }

    /**
     * 处理游戏结束事件
     */
    @EventSubscribe(ExecutorSelector.userVirtualExecutor)
    public void onGameOver(GameOverEvent event) {
        log.info("收到游戏结束事件: roomId={}, winner={}", event.getRoomId(), event.getWinnerId());
        getRobotManager().onGameOver(event);
    }
}
