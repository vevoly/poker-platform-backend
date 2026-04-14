package com.pokergame.common.constants;

/**
 * 认证相关常量
 */
public final class AuthConstants {

    private AuthConstants() {}

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";
    public static final String TOKEN_PARAM = "token";
    public static final int UNAUTHORIZED_STATUS = 401;
    public static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
    public static final String RESPONSE_CODE_FIELD = "code";
    public static final String RESPONSE_MESSAGE_FIELD = "message";
}
