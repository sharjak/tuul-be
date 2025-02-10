package com.tuul.test.vehicle.port;

import com.tuul.test.vehicle.model.Vehicle;

import java.util.Optional;

public interface FetchVehiclePort {

    Optional<Vehicle> findByCode(String code);
}
