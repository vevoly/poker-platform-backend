package com.pokergame.test.util;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import static io.restassured.RestAssured.given;

@Slf4j
public class LoginUtil {

    @Value
    public static class LoginResult {
        String token;
        Long userId;
    }

    public static LoginResult login(String username, String password) {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;

        // 构造登录请求体（请根据实际 LoginReq 结构调整）
        String requestBody = String.format("""
                {
                    "username": "%s",
                    "password": "%s"
                }
                """, username, password);

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/user/login")
                .then()
                .statusCode(200)
                .extract()
                .response();

        int code = response.jsonPath().getInt("code");
        if (code == 200) {
            String token = response.jsonPath().getString("data.token");
            Long userId = response.jsonPath().getLong("data.userId");
            log.info("登录成功: {} (userId={})", username, userId);
            return new LoginResult(token, userId);
        } else {
            log.error("登录失败: {}, code={}", username, code);
            return null;
        }
    }
}
