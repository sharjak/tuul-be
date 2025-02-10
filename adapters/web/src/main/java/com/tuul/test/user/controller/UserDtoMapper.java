package com.tuul.test.user.controller;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;
import com.tuul.test.user.model.UserWithDetails;
import com.tuul.test.vehicle.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
interface UserDtoMapper {
    UserDto toDto(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "activeVehicleId", ignore = true)
    User toDomain(SaveUserDto saveUserDto);

    TokenDto toDto(Token token);

    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "activeVehicle", source = "activeVehicle", qualifiedByName = "mapActiveVehicle")
    UserDetailsDto toDto(UserWithDetails userWithDetails);

    @Named("mapActiveVehicle")
    default ActiveVehicleDto mapActiveVehicle(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new ActiveVehicleDto(vehicle.getCode());
    }
}
