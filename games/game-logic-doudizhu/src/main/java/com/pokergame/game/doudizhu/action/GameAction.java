package com.pokergame.game.doudizhu.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.exception.MsgException;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.game.GameType;

import com.pokergame.common.model.game.doudizhu.GrabLandlordReq;
import com.pokergame.common.model.room.PassReq;
import com.pokergame.common.model.room.PlayCardReq;
import com.pokergame.common.pattern.PatternRecognizer;
import com.pokergame.common.pattern.PatternRecognizerFactory;
import com.pokergame.common.rule.ValidationResult;

import com.pokergame.common.exception.GameCode;
import com.pokergame.game.doudizhu.bidding.BiddingManager;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.enums.InternalOperation;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.game.doudizhu.rule.DoudizhuRuleChecker;
import lombok.extern.slf4j.Slf4j;

/**
 * 游戏操作 Action
 *
 * 处理游戏相关的客户端请求：
 * - 准备
 * - 开始游戏（由 OperationHandler 处理）
 * - 抢地主
 * - 不抢地主
 * - 出牌
 * - 过牌
 *
 * @author poker-platform
 */
@Slf4j
@ActionController(DoudizhuCmd.CMD)
public class GameAction {

    private final DoudizhuRoomService roomService = DoudizhuRoomService.me();
    private final PatternRecognizer recognizer = PatternRecognizerFactory.get(GameType.DOUDIZHU);

    // ==================== 准备操作 ====================

    /**
     * 准备/取消准备
     *
     * @param ctx FlowContext
     */
    @ActionMethod(DoudizhuCmd.READY)
    public void ready(FlowContext ctx) {
        long userId = ctx.getUserId();

        DoudizhuRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 通过 OperationHandler 处理
        room.operation(InternalOperation.READY);
    }

    // ==================== 叫地主操作 ====================

    /**
     * 抢地主
     *
     * @param req 抢地主请求
     * @param ctx FlowContext
     */
    @ActionMethod(DoudizhuCmd.GRAB_LANDLORD)
    public void grabLandlord(GrabLandlordReq req, FlowContext ctx) {
        long userId = ctx.getUserId();

        DoudizhuRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 检查游戏状态
        DoudizhuGameStatus status = room.getGameStatus();
        GameCode.ILLEGAL_OPERATION.assertTrueThrows(status != DoudizhuGameStatus.BIDDING);

        // 获取叫地主管理器并处理
        BiddingManager biddingManager = room.getBiddingManager();
        GameCode.DOUDIZHU_BIDDING_NOT_INITIALIZED.assertTrueThrows(biddingManager == null);

        biddingManager.handleGrab(userId, req.getMultiple());
    }

    /**
     * 不抢地主
     *
     * @param ctx FlowContext
     */
    @ActionMethod(DoudizhuCmd.NOT_GRAB)
    public void notGrab(FlowContext ctx) {
        long userId = ctx.getUserId();

        DoudizhuRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 检查游戏状态
        DoudizhuGameStatus status = room.getGameStatus();
        GameCode.ILLEGAL_OPERATION.assertTrueThrows(status != DoudizhuGameStatus.BIDDING);

        // 获取叫地主管理器并处理
        BiddingManager biddingManager = room.getBiddingManager();
        GameCode.DOUDIZHU_BIDDING_NOT_INITIALIZED.assertTrueThrows(biddingManager == null);

        biddingManager.handleNotGrab(userId);
    }

    // ==================== 出牌操作 ====================

    /**
     * 出牌
     *
     * @param req 出牌请求
     * @param ctx FlowContext
     */
    @ActionMethod(DoudizhuCmd.PLAY_CARD)
    public void playCard(PlayCardReq req, FlowContext ctx) {
        long userId = ctx.getUserId();

        // 获取房间
        DoudizhuRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 检查游戏状态
        DoudizhuGameStatus status = room.getGameStatus();
        GameCode.GAME_NOT_STARTED.assertTrueThrows(status != DoudizhuGameStatus.PLAYING);

        // 检查是否为当前玩家
        GameCode.NOT_YOUR_TURN.assertTrueThrows(!room.isCurrentPlayer(userId));

        // 获取玩家
        DoudizhuPlayer player = room.getDoudizhuPlayer(userId);

        // 使用规则检查器校验出牌
        DoudizhuRuleChecker ruleChecker = new DoudizhuRuleChecker(room);
        ValidationResult result = ruleChecker.validatePlay(userId, req.getCards());

        if (!result.isValid()) {
            throw new MsgException(result.getErrorCode(), result.getErrorMessage());
        }

        // 移除玩家手牌
        player.removeCards(req.getCards());

        // 更新房间出牌记录
        room.updateLastPlay(userId, req.getCards(), result.getPattern(), result.getMainRank(), result.getSubRank());

        // 如果是炸弹/王炸，增加倍率
        if (result.isBombOrRocket()) {
            room.addBomb();
        }

        // 广播出牌
        DoudizhuBroadcastKit.broadcastPlayCard(userId, req.getCards(), room);

        // 检查游戏是否结束
        if (player.getCardCount() == 0) {
            log.info("玩家 {} 出完所有牌，游戏结束", userId);
            room.changeGameStatus(DoudizhuGameStatus.FINISHED);
            // TODO: 结算逻辑
            return;
        }

        // 切换到下一个玩家
        room.nextTurn();

        // 重置超时定时器
        if (room.getTurnManager() != null) {
            room.getTurnManager().resetTimeout();
        }
    }

    /**
     * 过牌
     *
     * @param req 过牌请求
     * @param ctx FlowContext
     */
    @ActionMethod(DoudizhuCmd.PASS)
    public void pass(PassReq req, FlowContext ctx) {
        long userId = ctx.getUserId();

        // 获取房间
        DoudizhuRoom room = roomService.getUserRoom(userId);
        GameCode.PLAYER_NOT_IN_ROOM.assertTrueThrows(room == null);

        // 检查游戏状态
        DoudizhuGameStatus status = room.getGameStatus();
        GameCode.GAME_NOT_STARTED.assertTrueThrows(status != DoudizhuGameStatus.PLAYING);

        // 检查是否为当前玩家
        GameCode.NOT_YOUR_TURN.assertTrueThrows(!room.isCurrentPlayer(userId));

        log.info("玩家 {} 过牌", userId);

        // 广播过牌
        DoudizhuBroadcastKit.broadcastPass(userId, room);

        // 切换到下一个玩家
        room.nextTurn();

        // 重置超时定时器
        if (room.getTurnManager() != null) {
            room.getTurnManager().resetTimeout();
        }
    }
}
