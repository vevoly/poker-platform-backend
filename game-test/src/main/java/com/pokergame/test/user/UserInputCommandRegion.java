package com.pokergame.test.user;

import com.iohao.game.external.client.AbstractInputCommandRegion;
import com.iohao.game.external.client.kit.ScannerKit;
import com.pokergame.common.cmd.UserCmd;
import com.pokergame.common.msg.UserInfo;
import com.pokergame.common.msg.UserLoginReq;
import com.pokergame.common.msg.UserLoginRes;
import lombok.extern.slf4j.Slf4j;

/**
 * 用户服测试区域
 * 对应 UserAction
 */
@Slf4j
public class UserInputCommandRegion extends AbstractInputCommandRegion {

    @Override
    public void initInputCommand() {
        // 设置主路由，与 UserAction 的主路由对应
        inputCommandCreate.cmd = UserCmd.CMD;

        // 添加登录请求模拟命令
        ofCommand(UserCmd.LOGIN)
                .setTitle("用户登录")
                .setRequestData(() -> {
                    // 动态输入用户名（控制台交互）
                    ScannerKit.log(() -> log.info("请输入用户名:"));
                    String username = ScannerKit.nextLine("testuser1");

                    ScannerKit.log(() -> log.info("请输入密码:"));
                    String password = ScannerKit.nextLine("123456");

                    UserLoginReq req = new UserLoginReq();
                    req.username = username;
                    req.password = password;
                    return req;
                })
                .callback(result -> {
                    UserLoginRes resp = result.getValue(UserLoginRes.class);
                    log.info("登录成功: {}", resp.userInfo);
                });

        // 添加获取用户信息请求
        ofCommand(UserCmd.GET_USER_INFO)
                .setTitle("获取用户信息")
                .setRequestData(() -> null)  // 无请求参数
                .callback(result -> {
                    var userInfo = result.getValue(UserInfo.class);
                    log.info("用户信息: {}", userInfo);
                });

        // 添加金币查询请求
//        ofCommand(UserCmd.CHECK_GOLD)
//                .setTitle("查询金币")
//                .setRequestData(() -> ClientUserContext.getUserId())
//                .callback(result -> {
//                    long gold = result.getValue(Long.class);
//                    log.info("当前金币: {}", gold);
//                });
    }
}
