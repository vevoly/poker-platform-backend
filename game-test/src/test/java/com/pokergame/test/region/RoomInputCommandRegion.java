package com.pokergame.test.region;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.command.CallbackDelegate;
import com.iohao.game.external.client.kit.ScannerKit;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.model.broadcast.*;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.player.PlayerInfoDTO;
import com.pokergame.common.model.room.*;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

/**
 * 房间操作命令区域（主路由 RoomCmd.CMD）
 * 职责：处理房间相关的命令（创建、加入、离开、准备、开始）以及监听房间相关的广播
 */
@Slf4j
public class RoomInputCommandRegion extends BaseInputCommandRegion {

    private static String currentRoomId = null;  // 当前所在房间ID，用于后续命令的默认值

    public RoomInputCommandRegion() {
        // 设置主路由为房间命令的主路由
        this.inputCommandCreate.cmd = RoomCmd.CMD;
    }

    @Override
    public void initInputCommand() {
        // ==================== 1. 监听房间相关的广播消息 ====================
        listenPublicBroadcast();

        // ==================== 2. 房间操作命令 ====================

        // 创建房间（服务端自动生成 roomId，客户端无需输入）
        ofCommand(RoomCmd.CREATE_ROOM)
                .setTitle("创建房间")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入最大玩家数(2-3):"));
                    int maxPlayers = ScannerKit.nextInt(3);
                    CreateRoomReq req = new CreateRoomReq();
                    req.setMaxPlayers(maxPlayers);
                    req.setPlayerName(null); // 昵称由服务端从 FlowContext 获取
                    return req;
                })
                .callback(result -> {
                    CreateRoomResp resp = result.getValue(CreateRoomResp.class);
                    currentRoomId = String.valueOf(resp.getRoomId());
                    log.info("房间创建成功: roomId={}, ownerId={}, maxPlayers={}, playerCount={}",
                            resp.getRoomId(), resp.getOwnerId(), resp.getMaxPlayers(), resp.getPlayerCount());
                    printRoomState();
                });

        // 加入房间（需要用户输入房间ID）
        ofCommand(RoomCmd.JOIN_ROOM)
                .setTitle("加入房间")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入房间ID:"));
                    long roomId = ScannerKit.nextLong();
                    JoinRoomReq req = new JoinRoomReq();
                    req.setRoomId(roomId);
                    req.setPlayerName(null);
                    currentRoomId = String.valueOf(roomId);
                    return req;
                })
                .callback(result -> {
                    JoinRoomResp resp = result.getValue(JoinRoomResp.class);
                    log.info("加入房间成功: roomId={}, playerCount={}, players={}",
                            resp.getRoomId(), resp.getPlayerCount(),
                            resp.getPlayers().stream().map(PlayerInfoDTO::getNickname).collect(Collectors.toList()));
                    printRoomState();
                });

        // 离开房间（服务端根据当前用户获取所在房间，无需 roomId）
        ofCommand(RoomCmd.LEAVE_ROOM)
                .setTitle("离开房间")
                .setRequestData(() -> new LeaveRoomReq())
                .callback(result -> {
                    log.info("离开房间成功");
                    currentRoomId = null;
                    printRoomState();

                });

        // 准备（服务端根据当前用户获取房间，无需 roomId）
        ofCommand(RoomCmd.READY)
                .setTitle("准备")
                .setRequestData(() -> {
                    ReadyRoomReq req = new ReadyRoomReq();
                    req.setReady(true);
                    return req;
                })
                .callback(result -> {
                    log.info("准备成功");
                    printRoomState();
                });

        // 开始游戏（房主专用，服务端自动校验权限，无需 roomId）
        ofCommand(RoomCmd.START_GAME)
                .setTitle("开始游戏")
                .setRequestData(StartGameReq::new)
                .callback(result -> {
                    log.info("游戏开始");
                    printRoomState();
                });

        // 托管（服务端根据当前用户获取所在房间，无需 roomId）
        ofCommand(RoomCmd.TRUSTEESHIP)
                .setTitle("托管(1开启/0关闭)")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入托管状态(1:开启,0:关闭):"));
                    int state = ScannerKit.nextInt(0);
                    TrusteeshipReq req = new TrusteeshipReq();
                    req.setTrusteeship(state == 1);
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    return req;
                })
                .callback(result -> {
                    log.info("托管状态设置成功");
                    printRoomState();
                });
    }

    @Override
    protected String getCurrentRoomId() {
        return currentRoomId;
    }

    @Override
    protected void setCurrentRoomId(String roomId) {
        currentRoomId = roomId;
    }

}
