package com.pokergame.gateway.hall.constants;

/**
 * API 路径常量
 * <p>定义系统中各服务的 HTTP API 路径，便于统一维护和复用。
 * 使用时可通过组合方式构建完整路径，例如：
 * <pre>{@code
 * @RequestMapping(ApiPathConstants.USER_MODULE_PATH + ApiPathConstants.USER_REGISTER)
 * public RegisterResp register(...) { ... }
 * }</pre>
 *
 * @author poker-platform
 */
public final class ApiPath {

    private ApiPath() {
        // 工具类，禁止实例化
    }

    // ==================== 用户模块 ====================

    /** 用户模块基础路径 */
    public static final String USER = "/api/user";

    /** 用户注册接口路径 */
    public static final String USER_REGISTER = "/register";

    /** 用户登录接口路径 */
    public static final String USER_LOGIN = "/login";

    /** 获取用户信息接口路径 */
    public static final String USER_INFO = "/info";

    /** 用户登出接口路径 */
    public static final String USER_LOGOUT = "/logout";

    // ==================== 货币模块 ====================

    public static final String CURRENCY = "/api/currency";

    /** 获取货币列表接口路径 */
    public static final String CURRENCY_LIST = "/list";

    // ==================== 健康检查模块 ====================

    /** 健康检查接口路径（用于 K8s 存活探针） */
    public static final String HEALTH_CHECK = "/actuator/health";

    /** 信息检查接口路径（用于 K8s 就绪探针） */
    public static final String INFO_CHECK = "/actuator/info";
}
