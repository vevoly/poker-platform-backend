package com.pokergame.starter.spring.annotation;

import com.pokergame.common.enums.GameActionType;
import com.pokergame.common.enums.GameEventType;
import com.pokergame.common.game.GameType;

import java.lang.annotation.*;

/**
 * 游戏事件发布注解
 * <p>
 * 标注在需要自动发布游戏事件的 Action 方法上，通过 AOP 切面在方法执行后自动构造并发布事件。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>
 * &#064;ActionMethod(DoudizhuCmd.GRAB_LANDLORD)
 * &#064;PublishGameEvent(
 *     type = GameEventType.DOUDIZHU_BID,
 *     roomIdSpel = "#roomId",
 *     userIdSpel = "#userId",
 *     multipleSpel = "#req.multiple"
 * )
 * public void grabLandlord(GrabLandlordReq req, FlowContext ctx) {
 *     // 原有业务逻辑
 * }
 * </pre>
 *
 * @author poker-platform
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublishGameEvent {

    /**
     * 事件类型（必填）
     */
    GameEventType eventType();

    /**
     * 游戏类型
     */
    GameType gameType();

    /** 动作类型（直接使用枚举，仅用于 GameActionEvent） */
    GameActionType actionType() default GameActionType.PLAY;

    /**
     * 房间 ID 的 SpEL 表达式（必填）
     * 示例："#roomId" 或 "#ctx.getRoomId()"
     */
    String roomIdSpel() default "#roomId";

    /**
     * 玩家 ID 的 SpEL 表达式（必填）
     * 示例："#userId" 或 "#req.playerId"
     */
    String userIdSpel() default "#userId";

    /**
     * 牌列表的 SpEL 表达式（可选，仅出牌事件需要）
     */
    String cardsSpel() default "";

    /**
     * 倍数的 SpEL 表达式（可选，仅叫地主事件需要）
     */
    String multipleSpel() default "";

    /**
     * 是否异步发布
     */
    boolean async() default true;
}
