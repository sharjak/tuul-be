package com.tuul.test.vehicle.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Vehicle Controller", description = "API for vehicle activities")
@RestController
@RequestMapping("vehicle")
@RequiredArgsConstructor
public class VehicleController {

    @Operation(summary = "Pair a vehicle with user", description = "Pairs the vehicle with code to user and returns the vehicle data.")
    @PostMapping("pair")
    ResponseEntity<Void> pairVehicle(@RequestBody @Valid PairVehicleDto pairVehicleDto) {
        // TODO: implement pairing with vehicles service
        return ResponseEntity.ok().build();
    }

}
