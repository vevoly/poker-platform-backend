package com.pokergame.test.ws;

import com.pokergame.test.util.WebSocketClient;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

import static io.restassured.RestAssured.given;

@Slf4j
public class WebSocketLoginTest {

    public static void main(String[] args) throws UnsupportedEncodingException {
        if (WebSocketClient.start("testuser1", "123456")) {
            log.info("WebSocket 测试客户端已启动，输入命令 login 进行 WebSocket 登录");
            return;
        }
        log.error("WebSocket 测试客户端登录失败");
    }

}
