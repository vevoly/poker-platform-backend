package com.pokergame.game.doudizhu.broadcast;

import com.baidu.bjf.remoting.protobuf.annotation.ProtobufClass;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.game.GameType;
import com.pokergame.common.model.broadcast.BaseBroadcastData;
import com.pokergame.core.base.BaseBroadcastKit;
import com.pokergame.game.doudizhu.room.DoudizhuPlayer;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 斗地主广播工具，封装斗地主特有的广播消息。
 * 复用 BaseBroadcastKit 发送广播，广播数据都继承 BaseBroadcastData 并自动设置 gameType。
 */
@Slf4j
public final class DoudizhuBroadcastKit {

    private DoudizhuBroadcastKit() {}

    private static final int GAME_TYPE = GameType.DOUDIZHU.getCode();

    /**
     * 广播玩家准备状态
     */
    public static void broadcastReady(DoudizhuPlayer player, boolean ready, DoudizhuRoom room) {
        ReadyBroadcastData data = new ReadyBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(player.getUserId());
        data.setReady(ready);
        data.setNickname(player.getNickname());

        BaseBroadcastKit.broadcastToRoom(room, RoomCmd.CMD, RoomCmd.READY_BROADCAST, data);
    }

    /**
     * 广播玩家进入房间
     */
    public static void broadcastEnterRoom(DoudizhuPlayer player, DoudizhuRoom room) {
        EnterRoomBroadcastData data = new EnterRoomBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(player.getUserId());
        data.setNickname(player.getNickname());
        data.setPlayerCount(room.getPlayerCount());
        data.setMaxPlayers(room.getMaxPlayers());

        BaseBroadcastKit.broadcastToRoom(room, RoomCmd.CMD, RoomCmd.ENTER_ROOM_BROADCAST, data);
    }

    /**
     * 广播玩家离开房间
     */
    public static void broadcastLeaveRoom(long userId, DoudizhuRoom room) {
        QuitRoomBroadcastData data = new QuitRoomBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(userId);
        data.setPlayerCount(room.getPlayerCount());

        BaseBroadcastKit.broadcastToRoom(room, RoomCmd.CMD, RoomCmd.QUIT_ROOM_BROADCAST, data);
    }

    /**
     * 广播游戏开始
     */
    public static void broadcastGameStart(DoudizhuRoom room) {
        GameStartBroadcastData data = new GameStartBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setRoomId(room.getRoomId());
        // 注意：players 列表中的每个 DoudizhuPlayer 可能需要转换为不含敏感信息的 DTO，这里简单传递，建议使用浅拷贝或只传必要字段
        data.setPlayers(room.getAllDoudizhuPlayers());

        BaseBroadcastKit.broadcastToRoom(room, RoomCmd.CMD, RoomCmd.GAME_START_BROADCAST, data);
    }

    /**
     * 广播出牌
     */
    public static void broadcastPlayCard(long playerId, List<Card> cards, DoudizhuRoom room) {
        PlayCardBroadcastData data = new PlayCardBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(playerId);
        data.setCards(cards);
        data.setRemainingCards(room.getDoudizhuPlayer(playerId).getCardCount());

        BaseBroadcastKit.broadcastToRoom(room, DoudizhuCmd.CMD, DoudizhuCmd.PLAY_CARD_BROADCAST, data);
    }

    /**
     * 广播过牌
     */
    public static void broadcastPass(long userId, DoudizhuRoom room) {
        PassBroadcastData data = new PassBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(userId);

        BaseBroadcastKit.broadcastToRoom(room, DoudizhuCmd.CMD, DoudizhuCmd.PASS_BROADCAST, data);
    }

    /**
     * 广播抢地主
     */
    public static void broadcastGrabLandlord(long userId, int multiple, DoudizhuRoom room) {
        GrabLandlordBroadcastData data = new GrabLandlordBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(userId);
        data.setMultiple(multiple);

        BaseBroadcastKit.broadcastToRoom(room, DoudizhuCmd.CMD, DoudizhuCmd.GRAB_LANDLORD_BROADCAST, data);
    }

    /**
     * 广播不抢地主
     */
    public static void broadcastNotGrab(long userId, DoudizhuRoom room) {
        NotGrabBroadcastData data = new NotGrabBroadcastData();
        data.setGameType(GAME_TYPE);
        data.setUserId(userId);

        BaseBroadcastKit.broadcastToRoom(room, DoudizhuCmd.CMD, DoudizhuCmd.NOT_GRAB_LANDLORD_BROADCAST, data);
    }

    /**
     * 广播叫地主回合
     */
    public static void broadcastBiddingTurn(long playerId, int round, DoudizhuRoom room) {
        BiddingTurnData data = new BiddingTurnData();
        data.setGameType(GAME_TYPE);
        data.setPlayerId(playerId);
        data.setRound(round);
        data.setTimeoutSeconds(15);

        BaseBroadcastKit.broadcastToRoom(room, DoudizhuCmd.CMD, DoudizhuCmd.BIDDING_TURN_BROADCAST, data);
    }

    // ==================== 广播数据类（都继承 BaseBroadcastData） ====================

    @Data
    @ProtobufClass
    public static class ReadyBroadcastData extends BaseBroadcastData {
        private long userId;
        private boolean ready;
        private String nickname;
    }

    @Data
    @ProtobufClass
    public static class EnterRoomBroadcastData extends BaseBroadcastData {
        private long userId;
        private String nickname;
        private int playerCount;
        private int maxPlayers;
    }

    @Data
    @ProtobufClass
    public static class QuitRoomBroadcastData extends BaseBroadcastData {
        private long userId;
        private int playerCount;
    }

    @Data
    @ProtobufClass
    public static class GameStartBroadcastData extends BaseBroadcastData {
        private long roomId;
        private List<DoudizhuPlayer> players;
    }

    @Data
    @ProtobufClass
    public static class PlayCardBroadcastData extends BaseBroadcastData {
        private long userId;
        private List<Card> cards;
        private int remainingCards;
    }

    @Data
    @ProtobufClass
    public static class PassBroadcastData extends BaseBroadcastData {
        private long userId;
    }

    @Data
    @ProtobufClass
    public static class GrabLandlordBroadcastData extends BaseBroadcastData {
        private long userId;
        private int multiple;
    }

    @Data
    @ProtobufClass
    public static class NotGrabBroadcastData extends BaseBroadcastData {
        private long userId;
    }

    @Data
    @ProtobufClass
    public static class BiddingTurnData extends BaseBroadcastData {
        private long playerId;
        private int round;
        private int timeoutSeconds;
    }
}
