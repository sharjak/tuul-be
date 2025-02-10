package com.tuul.test.user.controller;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface UserDtoMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    User toDomain(SaveUserDto saveUserDto);

    TokenDto toDto(Token token);
}
