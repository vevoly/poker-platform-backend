# Auth 服务设计
## 1. 概述
### 1.1 设计目标
- 统一认证：为 HTTP 和 WebSocket 两种接入方式提供统一的认证服务
- 多种登录方式：支持用户名密码、短信验证码、三方登录（微信/QQ/Google）
- Token 管理：集中管理 Token 的生成、验证、刷新、注销
- 安全可靠：支持单点登录、Token 过期、主动踢人
- 可扩展：新增登录方式只需添加新的 Provider
### 1.2 整体架构定位
```mermaid
flowchart TB
    subgraph Client[客户端层]
        C[游戏客户端]
    end

    subgraph Access[接入层]
        HTTP[大厅 HTTP 服务<br/>game-gateway-http]
        WS[游戏对外服<br/>game-external]
    end

    subgraph Gateway[网关层]
        Broker[Broker 集群<br/>内部路由]
    end

    subgraph Auth[鉴权服务层]
        AuthSvc[Auth 服务<br/>service-auth]
    end

    subgraph Business[业务服务层]
        User[用户服务]
        Game[游戏逻辑服]
    end

    C -->|HTTP 请求| HTTP
    C -->|WebSocket| WS
    HTTP --> Broker
    WS --> Broker
    Broker --> AuthSvc
    Broker --> User
    Broker --> Game
```
## 2. HTTP 认证架构
### 2.1 架构图
```mermaid
flowchart TB
    subgraph Client[客户端]
        C[游戏客户端]
    end

    subgraph Gateway[大厅 HTTP 服务]
        direction TB
        Filter[AuthFilter<br/>Token 提取与验证]
        
        subgraph Controller[Controller 层]
            Login[登录接口<br/>无需 Token]
            UserInfo[用户信息接口<br/>需要 Token]
            Other[其他业务接口<br/>需要 Token]
        end
    end

    subgraph Broker[Broker]
        B[内部路由]
    end

    subgraph Auth[Auth 服务]
        direction TB
        Verify[Token 验证 RPC]
        Generate[Token 生成 RPC]
        Store[(Token 存储)]
    end

    subgraph Business[业务服务]
        User[用户服务]
    end

    %% 登录流程（无 Token）
    C -->|1. POST /login<br/>username/password| Login
    Login -->|2. RPC 调用| B
    B -->|3. 路由| User
    User -->|4. RPC 调用生成Token| B
    B -->|5. 路由| Generate
    Generate -->|6. 存储| Store
    Generate -->|7. 返回 Token| B
    B -->|8. 返回 Token| User
    User -->|9. 返回 LoginResp| B
    B -->|10. 返回| Login
    Login -->|11. 返回 Token| C

    %% 业务请求流程（携带 Token）
    C -->|12. GET /user/info<br/>Header: Bearer token| Filter
    Filter -->|13. 提取 Token| Filter
    Filter -->|14. RPC 验证| B
    B -->|15. 路由| Verify
    Verify -->|16. 查询| Store
    Verify -->|17. 返回 userId| B
    B -->|18. 返回| Filter
    Filter -->|19. 存入上下文| Filter
    Filter -->|20. 放行| UserInfo
    UserInfo -->|21. RPC 转发 + userId| B
    B -->|22. 路由| User
    User -->|23. 返回数据| B
    B -->|24. 返回| UserInfo
    UserInfo -->|25. 返回响应| C
```
### 2.2 HTTP 认证流程说明
| 步骤 | 组件 | 说明 |
| :--- | :--- | :--- |
| **登录阶段** | | |
| **1-2** | **客户端 → 大厅 HTTP 服务** | **发起登录**：客户端通过 POST 请求发送加密后的用户名与密码。 |
| **3-5** | **大厅 → 用户服 → Auth服** | **内部中转**：大厅服务通过 Broker 转发至用户服，用户服协同 Auth 服务执行凭证校验。 |
| **6-7** | **Auth 服务** | **凭证签发**：验证通过后生成 JWT Token，并将映射关系同步至 **Redis** 缓存。 |
| **8-11** | **Auth 服务 → 客户端** | **响应回传**：生成的 Token 沿原链路逐层返回，客户端本地持久化存储。 |
| **业务请求阶段** | | |
| **12-13** | **客户端 → 大厅 HTTP 服务** | **携带凭证**：后续业务请求在 Header 中携带 `Authorization: Bearer {token}`。 |
| **14-18** | **大厅 HTTP 服务 → Auth 服务** | **在线验权**：大厅网关拦截请求并调用 Auth 服务，从 Redis/JWT 中解出 `userId`。 |
| **19-20** | **大厅 HTTP 服务** | **上下文注入** | 验证成功后将 `userId` 存入 Request Context，对请求进行放行。 |
| **21-25** | **大厅 → 用户服务** | **逻辑执行** | 转发请求至业务服（携带已校验的 `userId`），获取结果并返回客户端。 |

### 2.3 Token 传递方式
```http
# 登录响应
HTTP/1.1 200 OK
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "expireTime": 1734567890000,
  "userId": 1001
}

# 业务请求
GET /api/user/info HTTP/1.1
Host: game-gateway:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json
```
## 3. WebSocket 认证架构
### 3.1 架构图
```mermaid
flowchart TB
    subgraph Client[客户端]
        C[游戏客户端]
    end

    subgraph Gateway[游戏对外服]
        direction TB
        WS[WebSocket 服务<br/>端口 10100]
        Handler[WebSocketVerifyHandler<br/>连接验证器]
        Session[UserSession<br/>连接会话管理]
    end

    subgraph Broker[Broker]
        B[内部路由]
    end

    subgraph Auth[Auth 服务]
        direction TB
        Verify[Token 验证 RPC]
        Store[(Token 存储)]
    end

    subgraph Business[业务服务]
        Game[斗地主逻辑服]
    end

    %% 连接建立阶段
    C -->|1. WebSocket 握手<br/>ws://host?token=xxx| WS
    WS -->|2. 提取 Token| Handler
    Handler -->|3. RPC 验证 Token| B
    B -->|4. 路由| Verify
    Verify -->|5. 查询| Store
    Verify -->|6. 返回 userId| B
    B -->|7. 返回| Handler
    Handler -->|8. 绑定 userId 到 Session| Session
    Handler -->|9. 连接成功| C

    %% 游戏业务阶段
    C -->|10. 游戏业务请求<br/>（无需再传 Token）| WS
    WS -->|11. 从 Session 获取 userId| Session
    WS -->|12. 转发请求 + userId| B
    B -->|13. 路由| Game
    Game -->|14. 执行业务逻辑| Game
    Game -->|15. 返回结果| B
    B -->|16. 返回| WS
    WS -->|17. 返回响应| C
```
### 3.2 WebSocket 认证流程说明
| 阶段 | 步骤 | 说明 |
| :--- | :--- | :--- |
| **连接建立** | | |
| **1-2** | **客户端 → 游戏对外服** | **发起握手**：建立 WebSocket 连接，Token 通常通过 URL Query 参数（如 `ws://host:port?token=xxx`）传递。 |
| **3-7** | **网关 → Auth 服务** | **握手拦截**：网关在协议升级阶段暂停，通过 RPC 向 Auth 服务校验 Token 的合法性及有效期。 |
| **8** | **游戏对外服** | **身份绑定** | 验证通过后，将解出的 `userId` 存储在长连接的 `Session` 上下文中，作为后续通信的唯一标识。 |
| **9** | **网关 → 客户端** | **确认连接**：完成握手响应，正式建立全双工实时通信链路。 |
| **游戏业务** | | |
| **10-11** | **客户端 → 游戏对外服** | **极简请求**：后续所有操作（如出牌、抢地主）直接发送 Proto 内容，无需重复携带 Token，降低流量损耗。 |
| **12-17** | **网关 → 逻辑服** | **透明转发**：网关从当前 Session 直接提取 `userId` 并注入请求包，转发至逻辑服（如斗地主服）处理。 |

### 3.3 WebSocket 连接 URL
```text
# 连接 URL（Token 放在参数中）
ws://game-gateway:10100?token=eyJhbGciOiJIUzI1NiIs...

# 连接成功后，后续业务消息不再需要携带 Token
```
## 4. Auth 服务内部架构
### 4.1 模块结构
```mermaid
flowchart TB
    subgraph Auth[Auth 服务]
        direction TB
        
        subgraph Action[RPC Action 层]
            AuthAction[AuthAction<br/>RPC 入口]
        end
        
        subgraph Provider[认证提供者层]
            Base[BaseAuthenticationProvider<br/>抽象基类]
            Pwd[PasswordProvider<br/>用户名密码]
            Sms[SmsProvider<br/>短信验证码]
            OAuth[OAuthProvider<br/>三方登录]
        end
        
        subgraph Token[Token 管理层]
            Generator[TokenGenerator<br/>JWT 生成]
            Enhancer[TokenEnhancer<br/>Token 增强]
            Store[TokenStore<br/>Redis 存储]
        end
        
        subgraph Handler[事件处理层]
            Success[SuccessHandler<br/>认证成功]
            Failure[FailureHandler<br/>认证失败]
        end
        
        AuthAction --> Provider
        Provider --> Token
        Provider --> Handler
    end

    subgraph Storage[存储层]
        Redis[(Redis)]
        MySQL[(MySQL)]
    end

    Store --> Redis
    Pwd --> MySQL
    OAuth --> MySQL
```
### 4.2 提供的 RPC Action
| Action | 功能 | 调用方 |
| :--- | :--- | :--- |
| **passwordLogin** | **密码登录**：验证账号密码，成功后签发并存储 Token。 | 用户服务 (User Service) |
| **smsLogin** | **短信登录**：验证手机验证码，实现快速注册或登录。 | 用户服务 (User Service) |
| **oauthLogin** | **三方登录**：集成微信、QQ 等第三方平台身份认证。 | 用户服务 (User Service) |
| **verifyToken** | **身份验证**：校验 Token 合法性并返回对应的 `userId`。 | 大厅 HTTP / 游戏对外服 |
| **refreshToken** | **令牌续期**：延长现有 Token 有效期，保持用户在线状态。 | 用户服务 (User Service) |
| **logout** | **安全注销**：主动作废当前 Token 并清理 Redis 映射。 | 用户服务 (User Service) |
| **kickUser** | **强制下线**：从服务端强制失效指定用户的 Token 并断开连接。 | 管理后台 (Admin Console) |
### 4.3 数据存储设计
| 存储 (Store) | 内容 (Content) | Key 格式 | TTL | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| **Redis** | **Token → userId** | `auth:token:{token}` | 24h | **身份鉴权**：网关或大厅通过此 Key 快速定位玩家 ID。 |
| **Redis** | **userId → Token** | `auth:user:token:{userId}` | 24h | **单点登录**：用于互踢逻辑，确保一个账号只有一个有效 Token。 |
| **Redis** | **验证码** | `auth:sms:code:{mobile}` | 5min | **安全校验**：存储短信登录时的临时随机码。 |
| **MySQL** | **用户信息** | `user` 表 | - | **持久化资料**：存储账号、密码哈希（BCrypt）、金币等基础数据。 |
| **MySQL** | **三方绑定** | `user_oauth` 表 | - | **关系映射**：存储微信/QQ OpenID 与系统内 `userId` 的关联。 |

## 5. 两种认证方式对比
| 维度 | HTTP 认证 | WebSocket 认证 |
| :--- | :--- | :--- |
| **Token 传递方式** | **Header**: `Authorization: Bearer token` | **URL 参数**: `?token=xxx` |
| **认证时机** | **每次请求**：由拦截器或过滤器在请求进入业务逻辑前处理。 | **连接建立时**：在 WebSocket 握手（Upgrade）阶段进行校验。 |
| **认证频率** | **高频**：每个业务请求都需要独立的验证过程。 | **低频**：属于一次性认证，连接建立后即视为合法。 |
| **用户信息存储** | **Request 上下文**：生命周期仅限于单次 HTTP 请求。 | **Session**：存储在长连接会话中，随连接存续。 |
| **后续请求携带** | **必须携带**：由于 HTTP 无状态，后续请求仍需传 Token。 | **无需携带**：连接已绑定身份，后续消息包可极致精简。 |
| **调用 Auth 服务频率** | **高频**：每个请求都可能触发一次 RPC 鉴权调用。 | **极低**：仅在握手阶段触发一次 RPC 鉴权调用。 |

## 6. 完整数据流闭环
### 6.1 用户注册 → 登录 → 业务请求
```mermaid
sequenceDiagram
    participant C as 客户端
    participant HTTP as 大厅HTTP服务
    participant WS as 游戏对外服
    participant Broker as Broker
    participant Auth as Auth服务
    participant User as 用户服务
    participant Game as 游戏逻辑服

    Note over C,User: 1. 注册
    C->>HTTP: POST /api/user/register
    HTTP->>Broker: RPC 转发
    Broker->>User: 路由
    User->>User: 创建用户
    User-->>HTTP: 注册成功
    HTTP-->>C: 返回

    Note over C,Auth: 2. 登录（获取 Token）
    C->>HTTP: POST /api/user/login
    HTTP->>Broker: RPC 转发
    Broker->>User: 路由
    User->>Broker: 调用 Auth 生成 Token
    Broker->>Auth: 路由
    Auth->>Auth: 生成 Token，存储 Redis
    Auth-->>Broker: 返回 Token
    Broker-->>User: 返回
    User-->>HTTP: 返回 LoginResp
    HTTP-->>C: 返回 Token

    Note over C,Game: 3. HTTP 业务请求（携带 Token）
    C->>HTTP: GET /api/user/info + Token
    HTTP->>Broker: 验证 Token
    Broker->>Auth: 验证
    Auth-->>Broker: 返回 userId
    HTTP->>Broker: 转发请求 + userId
    Broker->>User: 路由
    User-->>HTTP: 返回数据
    HTTP-->>C: 返回响应

    Note over C,Game: 4. WebSocket 连接（携带 Token）
    C->>WS: WebSocket 握手 + Token
    WS->>Broker: 验证 Token
    Broker->>Auth: 验证
    Auth-->>Broker: 返回 userId
    WS->>WS: 绑定 userId 到连接
    WS-->>C: 连接成功

    Note over C,Game: 5. 游戏业务（无需 Token）
    C->>WS: 出牌请求
    WS->>WS: 从连接获取 userId
    WS->>Broker: 转发请求 + userId
    Broker->>Game: 路由
    Game-->>WS: 返回结果
    WS-->>C: 返回响应
```
### 6.2 Token 生命周期
```mermaid
stateDiagram-v2
    [*] --> 生成: 登录成功
    生成 --> 有效: 存储到 Redis
    
    有效 --> 有效: 业务请求验证通过
    有效 --> 刷新: 调用 refreshToken
    刷新 --> 生成: 生成新 Token
    生成 --> 注销: 登出/踢人/过期
    注销 --> [*]
    有效 --> 过期: 24小时未使用
    过期 --> [*]
```
## 7. 总结
| 组件 | 职责 | 对外暴露 |
| :--- | :--- | :--- |
| **大厅 HTTP 服务** | **Web 入口**：处理非实时请求（登录、商城、活动），执行 Token 验证并分发业务。 | **HTTP 8080** |
| **游戏对外服** | **长连接网关**：负责 WebSocket 接入、Session 维护、身份绑定及实时指令下发。 | **WebSocket 10100** |
| **Broker** | **中枢路由**：ioGame 核心，负责所有内网服务的注册、发现与消息通信路由。 | 内部 RPC |
| **Auth 服务** | **安全中心**：统一管理 Token 生命周期，提供跨服务的鉴权与用户信息校验接口。 | 内部 RPC |
| **用户服务** | **账户中心**：处理用户属性变更、资产结算、金币流水及持久化存储逻辑。 | 内部 RPC |
| **游戏逻辑服** | **对局大脑**：运行斗地主等具体游戏逻辑，处理发牌、出牌、结算等对局流程。 | 内部 RPC |
### 7.1 关键设计决策
| 决策 (Decision) | 说明 (Explanation) |
| :--- | :--- |
| **Auth 服务不对外暴露 HTTP** | **内部隔离**：所有认证请求均通过内部 RPC 调用，不直接面向公网，安全性更高。 |
| **Token 验证在接入层完成** | **职责前置**：大厅 HTTP 服务和游戏对外服各自负责 Token 验证，确保非法请求在边缘层即被拦截。 |
| **两种认证方式统一** | **逻辑复用**：无论是 HTTP 还是 WebSocket 接入，最终都调用同一个 Auth 服务的 `verifyToken` RPC 接口。 |
| **Token 增强** | **性能优化**：可在 Token Payload 中冗余 `userCode`、`nickname` 等常用信息，减少后续业务的 RPC 查询次数。 |
| **支持多种登录方式** | **灵活扩展**：通过 **Provider 模式** 统一抽象登录逻辑，方便横向扩展密码、短信、三方（微信/QQ）等登录方式。 |