package com.pokergame.user.converter;

import com.pokergame.common.converter.BaseConverter;
import com.pokergame.common.model.user.UserCurrencyDTO;
import com.pokergame.user.entity.UserCurrencyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 货币转换器
 * MapStruct 会在编译期自动生成实现类
 */
@Mapper(componentModel = "spring")
public interface CurrencyConverter extends BaseConverter<UserCurrencyEntity, UserCurrencyDTO> {

    CurrencyConverter INSTANCE = Mappers.getMapper(CurrencyConverter.class);

    @Override
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "currencyType", target = "currencyType")
    @Mapping(source = "amount", target = "amount")
    UserCurrencyDTO toModel(UserCurrencyEntity entity);

    @Override
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "currencyType", target = "currencyType")
    @Mapping(source = "amount", target = "amount")
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    UserCurrencyEntity toEntity(UserCurrencyDTO model);
}
