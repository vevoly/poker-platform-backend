package com.pokergame.common.converter;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 可转换接口，定义对象自身转换为 DTO 的能力。
 * <p>
 * 该接口适用于轻量级、单向的转换场景，如领域对象转换为传输对象（DTO），
 * 特别是在跨服务事件传递时，将内部对象转为可序列化的简单 DTO。
 * </p>
 * <p>
 * 与 {@link com.pokergame.common.convert.BaseConverter} 的区别：
 * <ul>
 *   <li>{@code Convertible} 由被转换的对象本身实现，仅提供单向转换（自身 → DTO），适用于简单场景；</li>
 *   <li>{@code BaseConverter} 是独立的转换器接口，支持双向转换（Entity ↔ Model）、批量转换、更新映射等复杂需求，通常配合 MapStruct 使用。</li>
 * </ul>
 * </p>
 *
 * @param <T> DTO 类型
 */
public interface Convertible<T> {

    /**
     * 将当前对象转换为对应的 DTO 实例。
     *
     * @return DTO 对象
     */
    T toDTO();

    /**
     * 批量将可转换对象列表转换为 DTO 列表。
     * <p>
     * 示例：
     * <pre>
     * List&lt;Card&gt; cards = ...;
     * List&lt;CardDTO&gt; dtos = Convertible.toDTOList(cards);
     * </pre>
     * </p>
     *
     * @param source 可转换对象列表
     * @param <R>    DTO 类型
     * @param <C>    可转换对象类型（实现了 Convertible）
     * @return DTO 列表，如果输入为 null 则返回 null
     */
    static <R, C extends Convertible<R>> List<R> toDTOList(List<C> source) {
        if (source == null) {
            return null;
        }
        return source.stream()
                .map(Convertible::toDTO)
                .collect(Collectors.toList());
    }
}
