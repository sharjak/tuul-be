package com.tuul.test.vehicle.port;

import com.tuul.test.vehicle.model.Vehicle;

import java.util.Optional;
import java.util.UUID;

public interface FetchVehiclePort {

    Optional<Vehicle> findByCode(String code);

    Optional<Vehicle> fetch(UUID id);
}
