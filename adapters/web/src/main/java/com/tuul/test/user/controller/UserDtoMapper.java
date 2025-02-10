package com.tuul.test.user.controller;

import com.tuul.test.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper(componentModel = "spring")
interface UserDtoMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toDomain(SaveUserDto saveUserDto);
}
