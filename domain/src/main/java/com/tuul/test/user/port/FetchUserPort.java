package com.tuul.test.user.port;

import com.tuul.test.user.model.User;

import java.util.Optional;
import java.util.UUID;

public interface FetchUserPort {

    Optional<User> findByEmail(String email);

    Optional<User> fetch(UUID id);

    boolean existsByEmail(String email);

    boolean existsActiveVehicleById(UUID userId);

    boolean existsActiveVehicleUnderAnyUsers(UUID vehicleId);

    boolean existsActiveVehicleUnderUser(UUID userId, UUID vehicleId);
}
