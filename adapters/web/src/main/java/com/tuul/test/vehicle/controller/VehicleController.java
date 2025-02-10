package com.tuul.test.vehicle.controller;

import com.tuul.test.vehicle.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vehicle Controller", description = "API for vehicle activities")
@RestController
@RequestMapping("vehicle")
@RequiredArgsConstructor
public class VehicleController {
    private final VehicleService vehicleService;

    @Operation(summary = "Pair a vehicle with user", description = "Pairs the vehicle with code to user and returns the vehicle data.")
    @PostMapping("pair")
    public ResponseEntity<Void> pairVehicle(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PairVehicleDto pairVehicleDto) {

        vehicleService.pair(token, pairVehicleDto.code());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Unpair a vehicle from user", description = "Deletes the active vehicle under the user.")
    @DeleteMapping("pair")
    public ResponseEntity<Void> unpairVehicle(
            @RequestHeader("Authorization") String token,
            @RequestBody @Valid PairVehicleDto pairVehicleDto) {

        vehicleService.unpair(token, pairVehicleDto.code());
        return ResponseEntity.ok().build();
    }
}
