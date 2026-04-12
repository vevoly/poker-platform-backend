package com.pokergame.user.converter;

import com.pokergame.common.converter.BaseConverter;
import com.pokergame.common.model.user.CurrencyChangeLogDTO;
import com.pokergame.user.entity.CurrencyChangeLogEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface CurrencyChangeLogConverter extends BaseConverter<CurrencyChangeLogEntity, CurrencyChangeLogDTO> {
    CurrencyChangeLogConverter INSTANCE = Mappers.getMapper(CurrencyChangeLogConverter.class);

    // 可以不用显式写 toDto 和 toDtoList，因为继承后会自动生成，但需要指定映射
    // 如果有特殊字段需要映射，可以覆盖
    @Override
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "localDateTimeToLong")
    CurrencyChangeLogDTO toDTO(CurrencyChangeLogEntity entity);

    // 如果需要反向映射，也可以覆盖
    @Override
    @Mapping(target = "createTime", source = "createTime", qualifiedByName = "longToLocalDateTime")
    CurrencyChangeLogEntity toEntity(CurrencyChangeLogDTO dto);

}
