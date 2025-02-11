package com.tuul.test.vehicle.service;

import com.tuul.test.vehicle.model.VehicleCommand;

public interface VehicleService {

    void pair(String token, String code);

    void unpair(String token, String code);

    void sendCommand(String token, String code, VehicleCommand command);
}
