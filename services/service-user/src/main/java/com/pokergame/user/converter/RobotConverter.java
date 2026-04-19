package com.pokergame.user.converter;

import com.pokergame.common.converter.BaseConverter;
import com.pokergame.common.model.robot.RobotAccountDTO;
import com.pokergame.user.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RobotConverter extends BaseConverter<UserEntity, RobotAccountDTO> {

    RobotConverter INSTANCE = Mappers.getMapper(RobotConverter.class);

    /**
     * UserEntity → RobotAccountDTO
     */
    @Override
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "robotDifficulty", target = "difficulty")
    @Mapping(source = "robotEnabled", target = "enabled")
    RobotAccountDTO toDTO(UserEntity entity);

    @Override
    @Mapping(source = "id", target = "userId")
    @Mapping(source = "robotDifficulty", target = "difficulty")
    @Mapping(source = "robotEnabled", target = "enabled")
    List<RobotAccountDTO> toDTOList(List<UserEntity> entities);
}
