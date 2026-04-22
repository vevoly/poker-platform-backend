package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.kit.ScannerKit;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.player.PlayerInfo;
import com.pokergame.common.model.room.*;
import lombok.extern.slf4j.Slf4j;

import java.util.stream.Collectors;

/**
 * 房间操作命令区域（使用 RoomCmd）
 */
/**
 * 房间操作命令区域（主路由 = RoomCmd.CMD）
 */
@Slf4j
public class RoomInputCommandRegion extends AbstractInputCommandRegion {

    private static String currentRoomId = null;

    public RoomInputCommandRegion() {
        // 设置主路由
        this.inputCommandCreate.cmd = RoomCmd.CMD;
    }

    @Override
    public void initInputCommand() {
        // 创建房间（不再需要输入昵称，服务端从 FlowContext 获取）
        ofCommand(RoomCmd.CREATE_ROOM)
                .setTitle("创建房间")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入最大玩家数(2-3):"));
                    int maxPlayers = ScannerKit.nextInt(3);
                    CreateRoomReq req = new CreateRoomReq();
                    req.setMaxPlayers(maxPlayers);
                    // 昵称可以为空，服务端会从 ctx.getNickname() 获取
                    req.setPlayerName(null);
                    return req;
                })
                .callback(result -> {
                    CreateRoomResp resp = result.getValue(CreateRoomResp.class);
                    currentRoomId = String.valueOf(resp.getRoomId());
                    log.info("房间创建成功: roomId={}, ownerId={}, maxPlayers={}, playerCount={}",
                            resp.getRoomId(), resp.getOwnerId(), resp.getMaxPlayers(), resp.getPlayerCount());
                });

        // 加入房间
        ofCommand(RoomCmd.JOIN_ROOM)
                .setTitle("加入房间")
                .setRequestData(() -> {
                    ScannerKit.log(() -> log.info("请输入房间ID:"));
                    long roomId = ScannerKit.nextLong();
                    JoinRoomReq req = new JoinRoomReq();
                    req.setRoomId(roomId);
                    req.setPlayerName(null);  // 昵称可为空
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
        ofCommand(RoomCmd.LEAVE_ROOM)
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

        // 准备
        ofCommand(RoomCmd.READY)
                .setTitle("准备")
                .setRequestData(() -> {
                    ReadyRoomReq req = new ReadyRoomReq();
                    req.setReady(true);
                    return req;
                })
                .callback(result -> log.info("准备成功"));

        // 开始游戏
        ofCommand(RoomCmd.START_GAME)
                .setTitle("开始游戏")
                .setRequestData(() -> {
                    StartGameReq req = new StartGameReq();
                    if (currentRoomId != null) {
                        req.setRoomId(Long.parseLong(currentRoomId));
                    }
                    return req;
                })
                .callback(result -> log.info("游戏开始"));
    }
}
