package com.pokergame.test.doudizhu;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.kit.ScannerKit;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.cmd.WSCmd;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.game.doudizhu.GrabLandlordReq;
import com.pokergame.common.model.game.doudizhu.NotGrabLandlordReq;
import com.pokergame.common.model.player.PlayerInfo;
import com.pokergame.common.model.room.*;
import com.pokergame.common.model.ws.WsAttachment;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 斗地主测试区域（含 WebSocket 登录命令）
 */
@Slf4j
public class DoudizhuInputCommandRegion extends AbstractInputCommandRegion {

    private static String currentRoomId = null;
    private final String token;

    public DoudizhuInputCommandRegion(String token) {
        this.token = token;
        // 设置斗地主命令的主路由
        this.inputCommandCreate.cmd = DoudizhuCmd.CMD;
    }

    @Override
    public void initInputCommand() {
        // ========== 1. WebSocket 登录（必须最先执行） ==========
        // 自动发送 WebSocket 登录请求（使用 .execute() 立即执行）
        ofCommand(WSCmd.LOGIN)
                .setTitle("WebSocket登录")
                .setRequestData(() -> {
                    WsAttachment attachment = new WsAttachment();
                    attachment.setToken(token);
                    return attachment;
                })
                .callback(result -> {
                    log.info("WebSocket 登录成功，可以开始斗地主游戏！");
                });

        // ========== 2. 斗地主房间操作 ==========
        // 创建房间
        ofCommand(DoudizhuCmd.CREATE_ROOM)
                .setTitle("创建房间")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入最大玩家数(2-3):"));
                    int maxPlayers = ScannerKit.nextInt(3);
//                    ScannerKit.log(() -> log.info("请输入玩家昵称:"));
//                    String nickname = ScannerKit.nextLine("测试玩家");
                    CreateRoomReq req = new CreateRoomReq();
                    req.setMaxPlayers(maxPlayers);
//                    req.setPlayerName(nickname);
                    return req;
                })
                .callback(result -> {
                    CreateRoomResp resp = result.getValue(CreateRoomResp.class);
                    currentRoomId = String.valueOf(resp.getRoomId());
                    log.info("房间创建成功: roomId={}, ownerId={}, maxPlayers={}, playerCount={}",
                            resp.getRoomId(), resp.getOwnerId(), resp.getMaxPlayers(), resp.getPlayerCount());
                });

        // 加入房间
        ofCommand(DoudizhuCmd.JOIN_ROOM)
                .setTitle("加入房间")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入房间ID:"));
                    long roomId = ScannerKit.nextLong();
//                    ScannerKit.log(() -> log.info("请输入玩家昵称:"));
//                    String nickname = ScannerKit.nextLine("测试玩家");
                    JoinRoomReq req = new JoinRoomReq();
                    req.setRoomId(roomId);
//                    req.setPlayerName(nickname);
                    currentRoomId = String.valueOf(roomId);
                    return req;
                })
                .callback(result -> {
                    JoinRoomResp resp = result.getValue(JoinRoomResp.class);
                    log.info("加入房间成功: roomId={}, playerCount={}, players={}",
                            resp.getRoomId(), resp.getPlayerCount(),
                            resp.getPlayers().stream().map(PlayerInfo::getNickname).collect(Collectors.toList()));
                });

        // 离开房间
        ofCommand(DoudizhuCmd.LEAVE_ROOM)
                .setTitle("离开房间")
                .setRequestData(() -> {
                    LeaveRoomReq req = new LeaveRoomReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    } else {
                        ScannerKit.log(() -> log.info("请输入房间ID:"));
                        req.setRoomId(ScannerKit.nextLong());
                    }
                    return req;
                })
                .callback(result -> {
                    log.info("离开房间成功");
                    currentRoomId = null;
                });

        // ========== 3. 斗地主游戏操作 ==========
        // 准备
        ReadyRoomReq readyRoomReq = new ReadyRoomReq();
        readyRoomReq.setReady(true);
        ofCommand(DoudizhuCmd.READY)
                .setTitle("准备")
                .setRequestData(() -> readyRoomReq)
                .callback(result -> log.info("准备成功"));

        // 开始游戏（房主使用）
        ofCommand(DoudizhuCmd.START_GAME)
                .setTitle("开始游戏")
                .setRequestData(() -> {
                    StartGameReq req = new StartGameReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    return req;
                })
                .callback(result -> log.info("游戏开始"));

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
