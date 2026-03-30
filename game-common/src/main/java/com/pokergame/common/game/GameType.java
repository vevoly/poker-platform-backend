package com.pokergame.common.game;

import lombok.Getter;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 游戏类型枚举
 *
 * @author poker-platform
 */
@Getter
public enum GameType {

    // ========== 基础游戏 ==========
    /** 所有游戏（用于通用牌型） */
    ALL(0, "通用", "Common", 0, 0),

    /** 斗地主 */
    DOUDIZHU(1, "斗地主", "Doudizhu", 3, 54),

    /** 德州扑克 */
    TEXAS(2, "德州扑克", "Texas Hold'em", 2, 52),

    /** 牛牛 */
    BULL(3, "牛牛", "Bull", 2, 52),

    /** 炸金花 */
    ZHAJINHUA(4, "炸金花", "ZhaJinHua", 2, 52),

    /** 十三水 */
    THIRTEEN(5, "十三水", "Thirteen", 2, 52),

    // ========== 麻将类 ==========
    /** 四川麻将 */
    MAHJONG_SICHUAN(10, "四川麻将", "Sichuan Mahjong", 4, 108),

    /** 广东麻将 */
    MAHJONG_GUANGDONG(11, "广东麻将", "Guangdong Mahjong", 4, 136),

    /** 台湾麻将 */
    MAHJONG_TAIWAN(12, "台湾麻将", "Taiwan Mahjong", 4, 144),

    // ========== 其他 ==========
    /** 掼蛋 */
    GUANDAN(20, "掼蛋", "Guandan", 4, 108),

    /** 升级 */
    UPGRADE(21, "升级", "Upgrade", 4, 108);

    /** 游戏编码 */
    private final int code;

    /** 中文名称 */
    private final String chineseName;

    /** 英文名称 */
    private final String englishName;

    /** 默认玩家人数 */
    private final int defaultPlayerCount;

    /** 标准牌数 */
    private final int standardCardCount;

    /** 是否已启用 */
    private boolean enabled = true;

    /** 扩展属性 */
    private final Map<String, Object> properties = new ConcurrentHashMap<>();

    GameType(int code, String chineseName, String englishName,
             int defaultPlayerCount, int standardCardCount) {
        this.code = code;
        this.chineseName = chineseName;
        this.englishName = englishName;
        this.defaultPlayerCount = defaultPlayerCount;
        this.standardCardCount = standardCardCount;
    }

    /**
     * 根据编码获取游戏类型
     */
    public static GameType fromCode(int code) {
        for (GameType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的游戏类型编码: " + code);
    }

    /**
     * 根据名称获取游戏类型
     */
    public static GameType fromName(String name) {
        for (GameType type : values()) {
            if (type.name().equalsIgnoreCase(name) ||
                    type.chineseName.equals(name) ||
                    type.englishName.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的游戏类型名称: " + name);
    }

    /**
     * 获取所有已启用的游戏
     */
    public static GameType[] getEnabledGames() {
        return Arrays.stream(values())
                .filter(GameType::isEnabled)
                .filter(g -> g != ALL)
                .toArray(GameType[]::new);
    }

    /**
     * 判断是否需要多副牌
     */
    public boolean isMultipleDecks() {
        return standardCardCount > 54;
    }

    /**
     * 判断是否为麻将类游戏
     */
    public boolean isMahjong() {
        return this.code >= 10 && this.code < 20;
    }

    /**
     * 判断是否为扑克类游戏
     */
    public boolean isPoker() {
        return this.code > 0 && this.code < 10;
    }

    /**
     * 获取最小玩家数
     */
    public int getMinPlayers() {
        switch (this) {
            case DOUDIZHU: return 2;
            case TEXAS: return 2;
            case BULL: return 2;
            case ZHAJINHUA: return 2;
            case THIRTEEN: return 2;
            case MAHJONG_SICHUAN: return 2;
            case MAHJONG_GUANGDONG: return 2;
            case MAHJONG_TAIWAN: return 2;
            case GUANDAN: return 2;
            case UPGRADE: return 2;
            default: return 2;
        }
    }

    /**
     * 获取最大玩家数
     */
    public int getMaxPlayers() {
        switch (this) {
            case DOUDIZHU: return 3;
            case TEXAS: return 9;
            case BULL: return 6;
            case ZHAJINHUA: return 6;
            case THIRTEEN: return 4;
            case MAHJONG_SICHUAN: return 4;
            case MAHJONG_GUANGDONG: return 4;
            case MAHJONG_TAIWAN: return 4;
            case GUANDAN: return 4;
            case UPGRADE: return 4;
            default: return 4;
        }
    }

    /**
     * 获取默认底注
     */
    public int getDefaultAnte() {
        switch (this) {
            case DOUDIZHU: return 10;
            case TEXAS: return 5;
            case BULL: return 5;
            case ZHAJINHUA: return 5;
            default: return 1;
        }
    }

    /**
     * 设置属性
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * 获取属性
     */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(String key) {
        return (T) properties.get(key);
    }

    /**
     * 启用/禁用游戏
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return String.format("%s(%d) - %s", chineseName, code, englishName);
    }
}
