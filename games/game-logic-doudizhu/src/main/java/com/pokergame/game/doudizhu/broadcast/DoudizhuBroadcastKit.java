package com.pokergame.game.doudizhu.broadcast;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 斗地主广播工具类
 *
 * 封装各种广播消息的发送逻辑
 *
 * 广播流程：
 * 1. 构建 CmdInfo（包含主路由和广播子路由）
 * 2. 通过房间的 CommunicationAggregationContext 发送广播
 *
 * @author poker-platform
 */
@Slf4j
public class DoudizhuBroadcastKit {

    /**
     * 广播玩家准备状态
     *
     * @param player 玩家
     * @param ready 是否准备
     * @param room 房间
     */
    public static void broadcastReady(DoudizhuPlayer player, boolean ready, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.READY_BROADCAST);

        // 构建广播数据
        ReadyBroadcastData data = new ReadyBroadcastData();
        data.setUserId(player.getUserId());
        data.setReady(ready);
        data.setNickname(player.getNickname());

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播准备状态: userId={}, ready={}", player.getUserId(), ready);
    }

    /**
     * 广播玩家进入房间
     *
     * @param player 玩家
     * @param room 房间
     */
    public static void broadcastEnterRoom(DoudizhuPlayer player, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.ENTER_ROOM_BROADCAST);

        EnterRoomBroadcastData data = new EnterRoomBroadcastData();
        data.setUserId(player.getUserId());
        data.setNickname(player.getNickname());
        data.setPlayerCount(room.getPlayerCount());
        data.setMaxPlayers(room.getMaxPlayers());

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播玩家进入房间: userId={}, roomId={}", player.getUserId(), room.getRoomId());
    }

    /**
     * 广播玩家离开房间
     *
     * @param userId 玩家ID
     * @param room 房间
     */
    public static void broadcastQuitRoom(long userId, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.QUIT_ROOM_BROADCAST);

        QuitRoomBroadcastData data = new QuitRoomBroadcastData();
        data.setUserId(userId);
        data.setPlayerCount(room.getPlayerCount());

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播玩家离开房间: userId={}", userId);
    }

    /**
     * 广播游戏开始
     *
     * @param room 房间
     */
    public static void broadcastGameStart(DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.GAME_START_BROADCAST);

        GameStartBroadcastData data = new GameStartBroadcastData();
        data.setRoomId(room.getRoomId());
        data.setPlayers(room.getAllDoudizhuPlayers());

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.info("广播游戏开始: roomId={}", room.getRoomId());
    }

    /**
     * 广播出牌
     *
     * @param playerId 出牌玩家
     * @param cards 出的牌
     * @param room 房间
     */
    public static void broadcastPlayCard(long playerId, List<Card> cards, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.PLAY_CARD_BROADCAST);

        PlayCardBroadcastData data = new PlayCardBroadcastData();
        data.setUserId(playerId);
        data.setCards(cards);
        data.setRemainingCards(room.getDoudizhuPlayer(playerId).getCardCount());

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播出牌: userId={}, cards={}", playerId, cards);
    }

    /**
     * 广播过牌
     */
    public static void broadcastPass(long userId, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.PASS_BROADCAST);

        PassBroadcastData data = new PassBroadcastData();
        data.setUserId(userId);

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播过牌: userId={}", userId);
    }

    /**
     * 广播抢地主
     */
    public static void broadcastGrabLandlord(long userId, int multiple, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.GRAB_LANDLORD_BROADCAST);

        GrabLandlordBroadcastData data = new GrabLandlordBroadcastData();
        data.setUserId(userId);
        data.setMultiple(multiple);

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播抢地主: userId={}, multiple={}", userId, multiple);
    }

    /**
     * 广播不抢地主
     */
    public static void broadcastNotGrab(long userId, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.NOT_GRAB_LANDLORD_BROADCAST);

        NotGrabBroadcastData data = new NotGrabBroadcastData();
        data.setUserId(userId);

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播不抢地主: userId={}", userId);
    }

    /**
     * 广播叫地主回合
     *
     * @param playerId 当前玩家ID
     * @param round 第几轮
     * @param room 房间
     */
    public static void broadcastBiddingTurn(long playerId, int round, DoudizhuRoom room) {
        CmdInfo cmdInfo = CmdInfo.of(DoudizhuCmd.cmd, DoudizhuCmd.BIDDING_TURN_BROADCAST);

        BiddingTurnData data = new BiddingTurnData();
        data.setPlayerId(playerId);
        data.setRound(round);
        data.setTimeoutSeconds(15);

        room.getAggregationContext().broadcast(cmdInfo, data);
        log.debug("广播叫地主回合: playerId={}, round={}", playerId, round);
    }



    // ==================== 广播数据类 ====================

    @Data
    public static class ReadyBroadcastData {
        private long userId;
        private boolean ready;
        private String nickname;
    }

    @Data
    public static class EnterRoomBroadcastData {
        private long userId;
        private String nickname;
        private int playerCount;
        private int maxPlayers;
    }

    @Data
    public static class QuitRoomBroadcastData {
        private long userId;
        private int playerCount;
    }

    @Data
    public static class GameStartBroadcastData {
        private long roomId;
        private List<DoudizhuPlayer> players;
    }

    @Data
    public static class PlayCardBroadcastData {
        private long userId;
        private List<Card> cards;
        private int remainingCards;
    }

    @Data
    public static class PassBroadcastData {
        private long userId;
    }

    @Data
    public static class GrabLandlordBroadcastData {
        private long userId;
        private int multiple;
    }

    @Data
    public static class NotGrabBroadcastData {
        private long userId;
    }

    @Data
    public static class BiddingTurnData {
        private long playerId;
        private int round;
        private int timeoutSeconds;
    }
}
