package com.pokergame.test.http;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 确保所有服务已启动：Broker、External、Auth、User、Hall
 */
@DisplayName("HTTP 接口集成测试")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HttpInterfaceTest {

    private static String authToken;
    private static Long userId;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
    }

    @Test
    @Order(1)
    @DisplayName("1. 用户注册")
    void testRegister() {
        String requestBody = """
                {
                    "username": "test00",
                    "password": "123456",
                    "nickname": "测试用户"
                }
                """;

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/user/register")
                .then()
                .statusCode(200)
                .extract()
                .response();

        // 检查统一响应格式
        int code = response.jsonPath().getInt("code");
        assertThat(code, equalTo(200));

        userId = response.jsonPath().getLong("data.userId");
        assertThat(userId, notNullValue());
        System.out.println("注册成功，userId: " + userId);
    }

    @Test
    @Order(2)
    @DisplayName("2. 用户登录")
    void testLogin() {
        String requestBody = """
                {
                    "username": "testuser1",
                    "password": "123456"
                }
                """;

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/user/login")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        int code = response.jsonPath().getInt("code");
        assertThat(code, equalTo(200));

        authToken = response.jsonPath().getString("data.token");
        userId = response.jsonPath().getLong("data.userId");

        assertThat(authToken, notNullValue());
        assertThat(authToken, not(emptyString()));
        assertThat(userId, notNullValue());
        System.out.println("登录成功，token: " + authToken);
    }

    @Test
    @Order(3)
    @DisplayName("3. 获取用户信息")
    void testGetUserInfo() {
        Response response = given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/user/info")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .response();

        int code = response.jsonPath().getInt("code");
        assertThat(code, equalTo(200));

        String username = response.jsonPath().getString("data.user.username");
        assertThat(username, equalTo("testuser1"));
        System.out.println("获取用户信息成功: " + response.asString());
    }

    @Test
    @Order(4)
    @DisplayName("4. 获取货币列表")
    void testGetCurrency() {
        Response response = given()
                .header("Authorization", "Bearer " + authToken)
                .when()
                .get("/api/currency/list")
                .then()
                .statusCode(200)
                .extract()
                .response();

        int code = response.jsonPath().getInt("code");
        assertThat(code, equalTo(200));

        assertThat(response.jsonPath().getList("data.currencies"), notNullValue());
        System.out.println("获取货币列表成功: " + response.asString());
    }

    @Test
    @Order(5)
    @DisplayName("5. 无效 Token 测试")
    void testInvalidToken() {
        given()
                .header("Authorization", "Bearer invalid_token")
                .when()
                .get("/api/user/info")
                .then()
                .statusCode(401);  // AuthFilter 直接返回 401，不经过 Result 包装
        System.out.println("无效 Token 测试通过");
    }

    @Test
    @Order(6)
    @DisplayName("6. 注册重复用户名（业务错误）")
    void testRegisterDuplicate() {
        String requestBody = """
                {
                    "username": "test001",
                    "password": "123456"
                }
                """;

        Response response = given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/api/user/register")
                .then()
                .statusCode(200)
                .extract()
                .response();

        int code = response.jsonPath().getInt("code");
        assertThat(code, equalTo(201003));  // USERNAME_EXISTS
        assertThat(response.jsonPath().getString("message"), equalTo("用户名已存在"));
        System.out.println("重复注册测试通过，返回错误码: " + code);
    }
}