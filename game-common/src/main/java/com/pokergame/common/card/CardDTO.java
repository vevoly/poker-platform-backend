package com.pokergame.common.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 牌数据传输对象
 * 用于跨服务事件传递，避免直接依赖游戏逻辑服内部的 Card 类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO implements Serializable {

    /** 牌ID（0-53） */
    private int id;

    /**
     * 根据ID快速构造
     */
    public static CardDTO of(int id) {
        return new CardDTO(id);
    }

    /**
     * 从游戏逻辑服的 Card 对象转换
     * 注意：此方法依赖于游戏逻辑服中的 Card 类，但通过反射或接口解耦
     * 推荐在游戏逻辑服中直接调用 Card.getId() 然后使用 CardDTO.of(id)
     */
    public static CardDTO fromGameCard(Object gameCard) {
        // 假设 gameCard 有 getId() 方法
        try {
            int id = (int) gameCard.getClass().getMethod("getId").invoke(gameCard);
            return new CardDTO(id);
        } catch (Exception e) {
            throw new IllegalArgumentException("Cannot convert game card to DTO", e);
        }
    }

    @Override
    public String toString() {
        return "CardDTO{" + id + "}";
    }
}
