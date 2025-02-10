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
public class ActiveVehicle {
    private UUID id;
    private UUID userId;
    private UUID vehicleId;
}
