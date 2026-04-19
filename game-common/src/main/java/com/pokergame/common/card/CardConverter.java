package com.pokergame.common.card;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 牌对象转换工具
 * 用于将游戏逻辑服内部的 Card 对象转换为可序列化的 CardDTO
 */
@Slf4j
public class CardConverter {

    private static final String GET_ID_METHOD = "getId";

    /**
     * 批量转换 Card 列表为 CardDTO 列表
     *
     * @param rawCards 原始 Card 对象列表
     * @return CardDTO 列表，如果输入为 null 则返回 null
     */
    public static List<CardDTO> toCardDTOList(List<?> rawCards) {
        if (rawCards == null) {
            return null;
        }
        return rawCards.stream()
                .map(CardConverter::toCardDTO)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 转换单个 Card 对象为 CardDTO
     *
     * @param card 原始 Card 对象
     * @return CardDTO，如果转换失败则返回 null
     */
    public static CardDTO toCardDTO(Object card) {
        if (card == null) {
            return null;
        }
        try {
            Method getId = card.getClass().getMethod(GET_ID_METHOD);
            int id = (int) getId.invoke(card);
            return CardDTO.of(id);
        } catch (Exception e) {
            log.error("card to CardDTO 转换失败, card class: {}", card.getClass().getName(), e);
            return null;
        }
    }
}