package com.pokergame.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 逻辑服枚举
 * <p>统一管理所有逻辑服的 ID、名称、标签
 *
 * @author poker-platform
 */
@Getter
@AllArgsConstructor
public enum LogicServer {

    /** 斗地主逻辑服 */
    GAME_DOUDIZHU("doudizhu-logic", "斗地主逻辑服", "doudizhu"),

    /** 牛牛逻辑服 */
    GAME_BULL("bull-logic", "牛牛逻辑服", "bull"),

    /** 德州逻辑服 */
    GAME_TEXAS("texas-logic", "德州逻辑服", "texas"),

    /** WebSocket逻辑服 */
    SERVICE_WS("ws-logic", "websocket逻辑服", "ws"),

    /** 认证逻辑服 */
    SERVICE_AUTH("auth-logic", "认证逻辑服", "auth"),

    /** 用户逻辑服 */
    SERVICE_USER("user-logic", "用户逻辑服", "user"),

    /** 联盟逻辑服 */
    SERVICE_ALLIANCE("alliance-logic", "联盟逻辑服", "alliance"),

    /** 匹配逻辑服 */
    SERVICE_MATCH("match-logic", "匹配逻辑服", "match"),

    /** 排行榜逻辑服 */
    SERVICE_RANK("rank-logic", "排行榜逻辑服", "rank"),

    /** 聊天逻辑服 */
    SERVICE_CHAT("chat-logic", "聊天逻辑服", "chat"),

    /** 网关-大厅 */
    GATEWAY_HALL("gateway-hall", "网关-大厅", "hall"),

    /** 网关-管理 */
    GATEWAY_ADMIN("gateway-admin", "网关-管理", "admin"),

    ;

    /** 逻辑服ID（基础标识，不含实例编号） */
    private final String id;

    /** 逻辑服名称 */
    private final String name;

    /** 逻辑服标签（用于分类和负载均衡） */
    private final String tag;

}
