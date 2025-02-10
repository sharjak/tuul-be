package com.tuul.test.vehicle.controller;

import jakarta.validation.constraints.NotBlank;

record PairVehicleDto(@NotBlank String code) {
}
