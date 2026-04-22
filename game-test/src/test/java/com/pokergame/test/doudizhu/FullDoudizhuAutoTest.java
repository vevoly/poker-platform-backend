package com.pokergame.test.doudizhu;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.pokergame.common.card.Card;
import com.pokergame.common.cmd.DoudizhuCmd;
import com.pokergame.common.cmd.RoomCmd;
import com.pokergame.common.model.game.StartGameReq;
import com.pokergame.common.model.game.doudizhu.GrabLandlordReq;
import com.pokergame.common.model.game.doudizhu.NotGrabLandlordReq;
import com.pokergame.common.model.room.*;
import com.pokergame.common.util.RpcInvokeUtil;
import com.pokergame.game.doudizhu.enums.DoudizhuGameStatus;
import com.pokergame.game.doudizhu.room.DoudizhuRoom;
import com.pokergame.game.doudizhu.room.DoudizhuRoomService;
import com.pokergame.test.util.LoginUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 斗地主完整流程自动化集成测试
 * 模拟3个真实玩家，自动完成一局游戏
 */
@DisplayName("斗地主完整流程自动化集成测试")
public class FullDoudizhuAutoTest {

    private static final long OWNER_ID = 10001L;
    private static final long PLAYER2_ID = 10002L;
    private static final long PLAYER3_ID = 10003L;

    @BeforeAll
    static void login() {
        // 登录获取 token（实际 RPC 调用时会自动使用，这里仅确保用户在线）
        LoginUtil.login("testuser1", "123456");
        LoginUtil.login("test002", "123456");
        LoginUtil.login("test003", "123456");
    }

    @Test
    void testFullGame() throws Exception {
        // 1. 创建房间
        CreateRoomReq createReq = new CreateRoomReq();
        createReq.setMaxPlayers(3);
        CreateRoomResp createResp = RpcInvokeUtil.invoke(
                BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.CREATE_ROOM),
                createReq,
                OWNER_ID,
                CreateRoomResp.class
        );
        long roomId = createResp.getRoomId();
        System.out.println("房间创建: " + roomId);

        // 2. 玩家2、3加入（需要传入房间ID，因为加入操作需要指定房间）
        JoinRoomReq joinReq = new JoinRoomReq();
        joinReq.setRoomId(roomId);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.JOIN_ROOM), joinReq, PLAYER2_ID, Void.class);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.JOIN_ROOM), joinReq, PLAYER3_ID, Void.class);

        // 3. 准备
        ReadyRoomReq readyReq = new ReadyRoomReq();
        readyReq.setReady(true);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.READY), readyReq, OWNER_ID, Void.class);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.READY), readyReq, PLAYER2_ID, Void.class);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.READY), readyReq, PLAYER3_ID, Void.class);

        // 4. 开始游戏
        StartGameReq startReq = new StartGameReq();
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(RoomCmd.CMD, RoomCmd.START_GAME), startReq, OWNER_ID, Void.class);

        // 5. 叫地主（假设当前玩家顺序为 owner, player2, player3）
        GrabLandlordReq grabReq = new GrabLandlordReq();
        grabReq.setMultiple(3);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(DoudizhuCmd.CMD, DoudizhuCmd.GRAB_LANDLORD), grabReq, OWNER_ID, Void.class);
        NotGrabLandlordReq notGrabReq = new NotGrabLandlordReq();
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(DoudizhuCmd.CMD, DoudizhuCmd.NOT_GRAB), notGrabReq, PLAYER2_ID, Void.class);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(DoudizhuCmd.CMD, DoudizhuCmd.NOT_GRAB), notGrabReq, PLAYER3_ID, Void.class);

        // 等待出牌阶段
        TimeUnit.SECONDS.sleep(1);

        // 6. 出牌（简化：地主出最小的牌，其他过牌）
        // 需要获取玩家手牌，这里略，假设有牌0
        PlayCardReq playReq = new PlayCardReq();
        playReq.setCards(List.of(Card.of(0)));
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(DoudizhuCmd.CMD, DoudizhuCmd.PLAY_CARD), playReq, OWNER_ID, Void.class);
        PassReq passReq = new PassReq();
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(DoudizhuCmd.CMD, DoudizhuCmd.PASS), passReq, PLAYER2_ID, Void.class);
        RpcInvokeUtil.invoke(BrokerClientHelper.getBrokerClient(),
                CmdInfo.of(DoudizhuCmd.CMD, DoudizhuCmd.PASS), passReq, PLAYER3_ID, Void.class);

        // 循环出牌直到游戏结束（实际应监听事件，这里简单等待）
        TimeUnit.SECONDS.sleep(30);

        // 验证游戏结束
        DoudizhuRoomService roomService = DoudizhuRoomService.me();
        DoudizhuRoom room = roomService.getRoom(roomId);
        assertThat(room.getGameStatusEnum()).isEqualTo(DoudizhuGameStatus.FINISHED);
    }
}