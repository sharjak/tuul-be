package com.tuul.test.vehicle.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    private UUID id;
    private String code;
    private double stateOfCharge;
    private double latitude;
    private double longitude;
    private boolean poweredOn;
    private double odometer;
    private double estimatedRange;
}
