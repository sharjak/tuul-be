package com.tuul.test.vehicle.service;

public interface VehicleService {

    void pair(String token, String code);

    void unpair(String token, String code);
}
