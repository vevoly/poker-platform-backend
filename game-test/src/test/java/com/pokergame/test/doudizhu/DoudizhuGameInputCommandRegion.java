package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.kit.ScannerKit;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.model.game.doudizhu.GrabLandlordReq;
import com.pokergame.common.model.game.doudizhu.NotGrabLandlordReq;
import com.pokergame.common.model.room.PassReq;
import com.pokergame.common.model.room.PlayCardReq;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.ArrayList;

/**
 * 斗地主游戏操作命令区域（主路由 = DoudizhuCmd.CMD）
 */
@Slf4j
public class DoudizhuGameInputCommandRegion extends AbstractInputCommandRegion {

    private static String currentRoomId = null;

    public DoudizhuGameInputCommandRegion() {
        this.inputCommandCreate.cmd = DoudizhuCmd.CMD;
    }

    public static void setCurrentRoomId(String roomId) {
        currentRoomId = roomId;
    }

    @Override
    public void initInputCommand() {
        // 抢地主
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

        // 不抢地主
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

        // 出牌
        ofCommand(DoudizhuCmd.PLAY_CARD)
                .setTitle("出牌")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入牌ID(用逗号分隔，如: 0,1,2):"));
                    String cardInput = ScannerKit.nextLine("0");
                    String[] cardIds = cardInput.split(",");
                    List<Card> cards = new ArrayList<>();
                    for (String id : cardIds) {
                        cards.add(Card.of(Integer.parseInt(id.trim())));
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
