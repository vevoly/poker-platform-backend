package com.pokergame.test.region;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.command.CallbackDelegate;
import com.iohao.game.external.client.kit.ScannerKit;
import com.pokergame.common.card.CardDTO;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.model.game.doudizhu.GrabLandlordReq;
import com.pokergame.common.model.game.doudizhu.NotGrabLandlordReq;
import com.pokergame.common.model.room.PassReq;
import com.pokergame.common.model.room.PlayCardReq;
import com.pokergame.game.doudizhu.broadcast.DoudizhuBroadcastKit;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

/**
 * 斗地主游戏操作命令区域（主路由 DoudizhuCmd.CMD）
 * 职责：处理斗地主专属命令（抢地主、出牌、过牌）以及监听斗地主游戏广播
 */
@Slf4j
public class DoudizhuGameInputCommandRegion extends AbstractInputCommandRegion {

    private static String currentRoomId = null;  // 当前房间ID，由 RoomInputCommandRegion 设置，这里需要共享

    public DoudizhuGameInputCommandRegion() {
        this.inputCommandCreate.cmd = DoudizhuCmd.CMD;
    }

    /**
     * 设置当前房间ID（供 RoomInputCommandRegion 调用，因为两个区域需要共享房间ID）
     */
    public static void setCurrentRoomId(String roomId) {
        currentRoomId = roomId;
    }

    @Override
    public void initInputCommand() {
        // ==================== 1. 监听斗地主游戏广播 ====================
        // 出牌广播
        ofListen((CallbackDelegate) result -> {
            DoudizhuBroadcastKit.PlayCardBroadcastData data = result.getValue(DoudizhuBroadcastKit.PlayCardBroadcastData.class);
            log.info("🃏 收到出牌广播: userId={}, cards={}, remaining={}",
                    data.getUserId(), data.getCards(), data.getRemainingCards());
        }, DoudizhuCmd.PLAY_CARD_BROADCAST, "出牌广播");

        // 叫地主回合广播
        ofListen((CallbackDelegate) result -> {
            DoudizhuBroadcastKit.BiddingTurnData data = result.getValue(DoudizhuBroadcastKit.BiddingTurnData.class);
            log.info("📢 收到叫地主回合广播: playerId={}, round={}, timeout={}s",
                    data.getPlayerId(), data.getRound(), data.getTimeoutSeconds());
        }, DoudizhuCmd.BIDDING_TURN_BROADCAST, "叫地主回合广播");

        // 抢地主广播
        ofListen((CallbackDelegate) result -> {
            DoudizhuBroadcastKit.GrabLandlordBroadcastData data = result.getValue(DoudizhuBroadcastKit.GrabLandlordBroadcastData.class);
            log.info("🔥 收到抢地主广播: userId={}, multiple={}", data.getUserId(), data.getMultiple());
        }, DoudizhuCmd.GRAB_LANDLORD_BROADCAST, "抢地主广播");

        // 不抢地主广播
        ofListen((CallbackDelegate) result -> {
            DoudizhuBroadcastKit.NotGrabBroadcastData data = result.getValue(DoudizhuBroadcastKit.NotGrabBroadcastData.class);
            log.info("❌ 收到不抢地主广播: userId={}", data.getUserId());
        }, DoudizhuCmd.NOT_GRAB_LANDLORD_BROADCAST, "不抢地主广播");

        // 过牌广播
        ofListen((CallbackDelegate) result -> {
            DoudizhuBroadcastKit.PassBroadcastData data = result.getValue(DoudizhuBroadcastKit.PassBroadcastData.class);
            log.info("⏸️ 收到过牌广播: userId={}", data.getUserId());
        }, DoudizhuCmd.PASS_BROADCAST, "过牌广播");

        // ==================== 2. 斗地主游戏操作命令 ====================

        // 抢地主（需要输入倍数，房间ID由服务端自动获取，但为了方便可提前设置）
        ofCommand(DoudizhuCmd.GRAB_LANDLORD)
                .setTitle("抢地主")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入倍数(1/2/3):"));
                    int multiple = ScannerKit.nextInt(3);
                    GrabLandlordReq req = new GrabLandlordReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    req.setMultiple(multiple);
                    return req;
                })
                .callback(result -> log.info("抢地主成功"));

        // 不抢地主（无额外参数）
        ofCommand(DoudizhuCmd.NOT_GRAB)
                .setTitle("不抢地主")
                .setRequestData(() -> {
                    NotGrabLandlordReq req = new NotGrabLandlordReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    return req;
                })
                .callback(result -> log.info("不抢地主"));

        // 出牌（需要输入牌ID列表，转换为 CardDTO）
        ofCommand(DoudizhuCmd.PLAY_CARD)
                .setTitle("出牌")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入牌ID(用逗号分隔，如: 0,1,2):"));
                    String cardInput = ScannerKit.nextLine("0");
                    String[] cardIds = cardInput.split(",");
                    List<CardDTO> cards = new ArrayList<>();
                    for (String id : cardIds) {
                        cards.add(CardDTO.of(Integer.parseInt(id.trim())));
                    }
                    PlayCardReq req = new PlayCardReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    req.setCards(cards);
                    return req;
                })
                .callback(result -> log.info("出牌成功"));

        // 过牌
        ofCommand(DoudizhuCmd.PASS)
                .setTitle("过牌")
                .setRequestData(() -> {
                    PassReq req = new PassReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    return req;
                })
                .callback(result -> log.info("过牌成功"));
    }
}
