package com.pokergame.test.doudizhu;

import com.iohao.game.external.client.InputCommandRegion;
import com.pokergame.test.region.DoudizhuGameInputCommandRegion;
import com.pokergame.test.region.RoomInputCommandRegion;
import com.pokergame.test.util.WebSocketClient;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HumanPlayerClient1 {

    public static void main(String[] args)  {
        List<InputCommandRegion> regions = List.of(
                new RoomInputCommandRegion(),
                new DoudizhuGameInputCommandRegion()
        );
        WebSocketClient.start("testuser1", "123456", regions);

        log.info("玩家1客户端已启动，命令示例：");
        log.info("  创建房间: 100-1");
        log.info("  准备: 100-10");
        log.info("  开始游戏: 100-11");
        log.info("  托管：100-12");
        log.info("  抢地主: 101-1");
        log.info("  出牌: 101-3");
        log.info("  过牌: 101-4");

        // 保持主线程运行（因为 ClientRunOne 启动后主线程会结束）
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
