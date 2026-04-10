package com.pokergame.common.converter;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * 通用转换器接口
 *
 * @param <E> Entity 类型
 * @param <M> Model 类型
 */
public interface BaseConverter<E, M> {

    /**
     * Entity → Model
     */
    M toModel(E entity);

    /**
     * Model → Entity
     */
    E toEntity(M model);

    /**
     * Entity List → Model List
     */
    List<M> toModelList(List<E> entityList);

    /**
     * Model List → Entity List
     */
    List<E> toEntityList(List<M> modelList);

    /**
     * 更新 Entity（从 Model）
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(M model, @MappingTarget E entity);

    // ==================== 通用类型转换方法 ====================

    /**
     * LocalDateTime → 时间戳（毫秒）
     *
     * <p>用于 Entity 转 DTO 时，将数据库时间类型转换为 RPC 传输友好的时间戳
     *
     * @param localDateTime LocalDateTime 对象
     * @return 毫秒时间戳，null 返回 null
     */
    default Long localDateTimeToLong(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * 时间戳（毫秒）→ LocalDateTime
     *
     * <p>用于 DTO 转 Entity 时，将 RPC 传输的时间戳转换为数据库友好的 LocalDateTime
     *
     * @param timestamp 毫秒时间戳
     * @return LocalDateTime 对象，null 或 0 返回 null
     */
    default LocalDateTime longToLocalDateTime(Long timestamp) {
        if (timestamp == null || timestamp == 0) {
            return null;
        }
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    // ==================== 可选：其他通用转换方法 ====================

    /**
     * String → Long（用于字符串ID转换）
     */
    default Long stringToLong(String str) {
        if (str == null || str.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Long → String
     */
    default String longToString(Long value) {
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * Integer → Boolean（0/1 转 boolean）
     */
    default Boolean intToBoolean(Integer value) {
        if (value == null) {
            return null;
        }
        return value == 1;
    }

    /**
     * Boolean → Integer
     */
    default Integer booleanToInt(Boolean value) {
        if (value == null) {
            return null;
        }
        return value ? 1 : 0;
    }
}
