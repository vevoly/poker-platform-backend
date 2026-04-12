package com.pokergame.user.converter;

import com.pokergame.common.converter.BaseConverter;
import com.pokergame.common.model.user.UserDTO;
import com.pokergame.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * 用户转换器
 * MapStruct 会在编译期自动生成实现类
 */
@Mapper(componentModel = "spring")
public interface UserConverter extends BaseConverter<UserEntity, UserDTO> {

    /**
     * 单例实例（非 Spring 环境下使用）
     */
    UserConverter INSTANCE = Mappers.getMapper(UserConverter.class);

    @Override
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "lastLoginTime", target = "lastLoginTime", qualifiedByName = "localDateTimeToLong")
    // 注意：UserDTO 中没有 password、createBy、updateBy、updateTime、delFlag、extra 字段
    // 所以不需要写 ignore，MapStruct 会自动忽略不存在的目标字段
    UserDTO toDTO(UserEntity entity);

    @Override
    @Mapping(source = "userId", target = "id")
    @Mapping(source = "lastLoginTime", target = "lastLoginTime", qualifiedByName = "longToLocalDateTime")
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    @Mapping(target = "delFlag", ignore = true)
    @Mapping(target = "extra", ignore = true)
    UserEntity toEntity(UserDTO model);

}
