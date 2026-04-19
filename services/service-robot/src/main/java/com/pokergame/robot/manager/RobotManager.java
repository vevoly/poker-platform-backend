package com.pokergame.robot.manager;

import com.iohao.game.action.skeleton.core.CmdInfo;
import com.iohao.game.bolt.broker.core.client.BrokerClientHelper;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.event.*;
import com.pokergame.common.model.robot.GetRobotAccountsResp;
import com.pokergame.common.model.robot.RobotAccountDTO;
import com.pokergame.common.util.RpcInvokeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 机器人账号管理器
 * <p>
 * 职责：
 * 1. 从用户服务（service-user）加载所有启用的机器人账号到内存池
 * 2. 提供随机获取机器人账号的能力
 * 3. 支持根据 userId 查询机器人账号
 * 4. 支持动态刷新账号池（如通过定时任务或事件通知）
 * </p>
 *
 * @author your-team
 * @since 1.0
 */
@Slf4j
@Component
public class RobotManager {

    /**
     * 内存账号池：key = 机器人用户ID, value = 机器人账号信息
     */
    private final Map<Long, RobotAccountDTO> robotPool = new ConcurrentHashMap<>();

    /**
     * 加载所有机器人账号到内存池
     * <p>
     * 调用用户服务的 RPC 接口获取所有 robot_enabled = true 的账号，
     * 并更新本地缓存。该方法会覆盖原有池内容。
     * </p>
     */
    public void loadRobotAccounts() {
        log.info("开始加载机器人账号...");
        try {
            // 调用用户服务的 RPC 接口，获取响应对象
            GetRobotAccountsResp resp = RpcInvokeUtil.invoke(
                    BrokerClientHelper.getBrokerClient(),
                    CmdInfo.of(UserCmd.CMD, UserCmd.GET_ROBOT_ACCOUNTS),
                    null,
                    GetRobotAccountsResp.class
            );

            if (resp == null || resp.getAccounts() == null) {
                log.warn("RPC 调用返回结果为空，账号池未更新");
                return;
            }

            List<RobotAccountDTO> robots = resp.getAccounts();
            // 清空旧数据并加载新数据
            robotPool.clear();
            for (RobotAccountDTO robot : robots) {
                // 只添加启用的机器人（接口已过滤，这里做二次保障）
                if (robot.getEnabled() != null && robot.getEnabled() == 1) {
                    robotPool.put(robot.getUserId(), robot);
                } else {
                    log.debug("跳过未启用的机器人: userId={}", robot.getUserId());
                }
            }
            log.info("成功加载 {} 个机器人账号到内存池", robotPool.size());
        } catch (Exception e) {
            log.error("加载机器人账号失败，请检查用户服务是否可用", e);
        }
    }

    /**
     * 随机获取一个可用的机器人账号
     *
     * @return 机器人账号信息（如果池为空则返回 Optional.empty()）
     */
    public Optional<RobotAccountDTO> getRandomRobot() {
        if (robotPool.isEmpty()) {
            log.warn("机器人账号池为空，无法获取随机机器人");
            return Optional.empty();
        }
        // 将 Map 的 values 转为列表后随机选取
        List<RobotAccountDTO> robots = new ArrayList<>(robotPool.values());
        int index = ThreadLocalRandom.current().nextInt(robots.size());
        return Optional.of(robots.get(index));
    }

    /**
     * 根据用户ID获取机器人账号
     *
     * @param userId 机器人用户ID
     * @return 机器人账号信息（可能为空）
     */
    public Optional<RobotAccountDTO> getRobotById(Long userId) {
        return Optional.ofNullable(robotPool.get(userId));
    }

    /**
     * 获取当前账号池中的机器人数量
     *
     * @return 机器人数量
     */
    public int getRobotCount() {
        return robotPool.size();
    }

    /**
     * 刷新账号池（等同于重新加载）
     * 可用于定时任务或手动触发更新
     */
    public void refresh() {
        loadRobotAccounts();
    }

    /**
     * 手动添加或更新一个机器人账号到内存池
     * （供管理接口调用，例如 GM 后台新增机器人后同步）
     *
     * @param robot 机器人账号信息
     */
    public void addOrUpdateRobot(RobotAccountDTO robot) {
        if (robot != null && robot.getUserId() != null) {
            robotPool.put(robot.getUserId(), robot);
            log.info("内存池中更新机器人: userId={}", robot.getUserId());
        }
    }

    /**
     * 从内存池中移除机器人账号
     *
     * @param userId 机器人用户ID
     */
    public void removeRobot(Long userId) {
        if (userId != null && robotPool.remove(userId) != null) {
            log.info("从内存池移除机器人: userId={}", userId);
        }
    }

    // ========== 房间状态管理（新增） ==========

    /** 房间状态映射：roomId -> RoomState */
    private final Map<String, RoomState> roomStateMap = new ConcurrentHashMap<>();

    /**
     * 获取房间状态
     */
    public RoomState getRoomState(String roomId) {
        return roomStateMap.get(roomId);
    }

    /**
     * 获取所有房间状态
     */
    public Map<String, RoomState> getAllRoomStates() {
        return Collections.unmodifiableMap(roomStateMap);
    }

    /**
     * 处理游戏开始事件
     */
    public void onGameStart(GameStartEvent event) {
        RoomState state = new RoomState()
                .setRoomId(event.getRoomId())
                .setGameType(event.getGameType())
                .setPlayerIds(event.getPlayerIds())
                .setScores(new ConcurrentHashMap<>())
                .setLastEventTime(System.currentTimeMillis());
        // 初始化分数
        for (Long pid : event.getPlayerIds()) {
            state.getScores().put(pid, event.getInitScore());
        }
        roomStateMap.put(event.getRoomId(), state);
        log.info("创建房间状态: roomId={}, gameType={}", event.getRoomId(), event.getGameType());
    }

    /**
     * 处理回合切换事件
     */
    public void onTurnChanged(TurnChangedEvent event) {
        RoomState state = roomStateMap.get(event.getRoomId());
        if (state == null) {
            log.warn("收到未注册房间的回合切换事件: roomId={}", event.getRoomId());
            return;
        }
        state.setCurrentPlayerId(event.getCurrentPlayerId());
        state.setTimeoutSeconds(event.getTimeoutSeconds());
        state.setLastEventTime(System.currentTimeMillis());

        // TODO: 如果当前玩家是机器人，触发决策（阶段4实现）
        // if (isRobot(state.getCurrentPlayerId())) {
        //     decisionEngine.decide(state);
        // }
    }

    /**
     * 处理发牌事件
     */
    public void onCardsDealt(CardsDealtEvent event) {
        RoomState state = roomStateMap.get(event.getRoomId());
        if (state == null) return;
        if (state.getHandCards() == null) {
            state.setHandCards(new ConcurrentHashMap<>());
        }
        state.getHandCards().put(event.getPlayerId(), event.getHandCards());
        state.setLastEventTime(System.currentTimeMillis());
    }

    /**
     * 处理叫地主/抢地主事件
     */
    public void onBidding(BiddingEvent event) {
        RoomState state = roomStateMap.get(event.getRoomId());
        if (state == null) return;
        state.setCurrentBidMultiple(event.getMultiple());
        state.setLastEventTime(System.currentTimeMillis());
        // 如果是机器人叫地主，触发决策
    }

    /**
     * 处理出牌事件
     */
    public void onCardPlayed(GameActionEvent event) {
        RoomState state = roomStateMap.get(event.getRoomId());
        if (state == null) return;
        if (state.getPlayedCards() == null) {
            state.setPlayedCards(new ConcurrentHashMap<>());
        }
        // 记录所有玩家打出的牌（用于记牌器）
        state.getPlayedCards().compute(event.getPlayerId(), (k, v) -> {
            if (v == null) v = new ArrayList<>();
            if (event.getCards() != null) v.addAll(event.getCards());
            return v;
        });
        state.setLastActionPlayerId(event.getPlayerId());
        state.setLastPlayedCards(event.getCards());
        state.setLastPlayPattern(event.getPattern() != null ? event.getPattern().name() : null);
        state.setLastEventTime(System.currentTimeMillis());
    }

    /**
     * 处理过牌事件
     */
    public void onPass(PassEvent event) {
        RoomState state = roomStateMap.get(event.getRoomId());
        if (state == null) return;
        state.setLastActionPlayerId(event.getPlayerId());
        state.setLastEventTime(System.currentTimeMillis());
    }

    /**
     * 处理游戏结束事件
     */
    public void onGameOver(GameOverEvent event) {
        roomStateMap.remove(event.getRoomId());
        log.info("清理房间状态: roomId={}, winner={}", event.getRoomId(), event.getWinnerId());
    }

    /**
     * 判断玩家是否为机器人
     */
    public boolean isRobot(long userId) {
        return robotPool.containsKey(userId);
    }

    /**
     * 获取机器人所在的房间ID列表（用于恢复等）
     */
    public List<String> getRobotRoomIds(long userId) {
        return roomStateMap.entrySet().stream()
                .filter(e -> e.getValue().getPlayerIds().contains(userId))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
