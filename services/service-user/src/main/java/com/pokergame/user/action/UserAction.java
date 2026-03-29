package com.pokergame.user.action;

import com.iohao.game.action.skeleton.annotation.ActionController;
import com.iohao.game.action.skeleton.annotation.ActionMethod;
import com.iohao.game.action.skeleton.core.flow.FlowContext;
import com.iohao.game.common.kit.RandomKit;
import com.iohao.game.common.kit.concurrent.TaskKit;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.msg.UserInfo;
import com.pokergame.common.msg.UserLoginReq;
import com.pokergame.common.msg.UserLoginRes;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 用户Action
 *
 * @author 游戏平台
 * @date 2024-03-26
 */
@Slf4j
@ActionController(UserCmd.CMD)
public class UserAction {

    /** 模拟用户数据存储 */
    private static final ConcurrentHashMap<Long, UserInfo> userStorage = new ConcurrentHashMap<>();

    static {
        // 初始化测试用户数据
        UserInfo user1 = new UserInfo();
        user1.userId = 1001L;
        user1.username = "testuser1";
        user1.nickname = "测试用户1";
        user1.gold = 1000;
        user1.level = 1;
        user1.exp = 0;
        userStorage.put(user1.userId, user1);

        UserInfo user2 = new UserInfo();
        user2.userId = 1002L;
        user2.username = "testuser2";
        user2.nickname = "测试用户2";
        user2.gold = 2000;
        user2.level = 2;
        user2.exp = 100;
        userStorage.put(user2.userId, user2);
    }

    /**
     * 用户登录
     */
    @ActionMethod(UserCmd.LOGIN)
    public UserLoginRes login(UserLoginReq req, FlowContext flowContext) {
        log.info("用户登录请求: username={}", req.username);

        // 模拟登录验证
        UserInfo user = findUserByUsername(req.username);

        UserLoginRes res = new UserLoginRes();

        if (user != null) {
            res.success = true;
            res.userInfo = user;
            log.info("用户登录成功: userId={}", user.userId);
        } else {
            res.success = false;
            res.errorMessage = "用户不存在";
            log.warn("用户登录失败: username={}", req.username);
        }

        return res;
    }

    /**
     * 获取用户信息
     */
    @ActionMethod(UserCmd.GET_USER_INFO)
    public UserInfo getUserInfo(FlowContext flowContext) {
        long userId = flowContext.getUserId();
        log.info("获取用户信息: userId={}", userId);

        UserInfo user = userStorage.get(userId);
        if (user == null) {
            // 创建默认用户信息
            user = createDefaultUser(userId);
            userStorage.put(userId, user);
        }

        return user;
    }

//    /**
//     * 扣除金币
//     */
//    @ActionMethod(UserCmd.DEDUCT_GOLD)
//    public ResponseMessage deductGold(FlowContext flowContext) {
//        long userId = flowContext.getUserId();
//        int amount = (Integer) flowContext.getData();
//
//        log.info("扣除金币: userId={}, amount={}", userId, amount);
//
//        UserInfo user = userStorage.get(userId);
//        if (user == null) {
//            return ResponseMessageBuilder.error("用户不存在");
//        }
//
//        if (user.gold < amount) {
//            return ResponseMessageBuilder.error("金币不足");
//        }
//
//        user.gold -= amount;
//        log.info("扣除金币成功: userId={}, newGold={}", userId, user.gold);
//
//        return ResponseMessageBuilder.ok();
//    }
//
//    /**
//     * 增加金币
//     */
//    @ActionMethod(UserCmd.ADD_GOLD)
//    public ResponseMessage addGold(FlowContext flowContext) {
//        long userId = flowContext.getUserId();
//        int amount = (Integer) flowContext.getData();
//
//        log.info("增加金币: userId={}, amount={}", userId, amount);
//
//        UserInfo user = userStorage.get(userId);
//        if (user == null) {
//            return ResponseMessageBuilder.error("用户不存在");
//        }
//
//        user.gold += amount;
//        log.info("增加金币成功: userId={}, newGold={}", userId, user.gold);
//
//        return ResponseMessageBuilder.ok();
//    }
//
//    /**
//     * 检查金币
//     */
//    @ActionMethod(UserCmd.CHECK_GOLD)
//    public ResponseMessage checkGold(FlowContext flowContext) {
//        long userId = flowContext.getUserId();
//        int requiredGold = (Integer) flowContext.getData();
//
//        log.info("检查金币: userId={}, requiredGold={}", userId, requiredGold);
//
//        UserInfo user = userStorage.get(userId);
//        if (user == null) {
//            return ResponseMessageBuilder.error("用户不存在");
//        }
//
//        boolean enough = user.gold >= requiredGold;
//        log.info("金币检查结果: userId={}, enough={}", userId, enough);
//
//        return ResponseMessageBuilder.ok(enough);
//    }

    /**
     * 模拟用户登录验证
     */
    private UserInfo findUserByUsername(String username) {
        // 模拟数据库查询延迟
        try {
            TimeUnit.MILLISECONDS.sleep(RandomKit.random(100, 500));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 简单的用户名验证
        for (UserInfo user : userStorage.values()) {
            if (user.username.equals(username)) {
                return user;
            }
        }
        return null;
    }

    /**
     * 创建默认用户
     */
    private UserInfo createDefaultUser(long userId) {
        UserInfo user = new UserInfo();
        user.userId = userId;
        user.username = "user" + userId;
        user.nickname = "玩家" + userId;
        user.gold = 100;
        user.level = 1;
        user.exp = 0;

        // 模拟异步保存用户数据
        TaskKit.executeVirtual(() -> {
            log.info("异步保存新用户数据: userId={}", userId);
            // 这里可以添加实际的数据库保存逻辑
        });

        return user;
    }
}
