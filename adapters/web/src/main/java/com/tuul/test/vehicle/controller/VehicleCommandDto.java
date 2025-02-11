package com.tuul.test.vehicle.controller;

import com.tuul.test.vehicle.model.VehicleCommand;
import jakarta.validation.constraints.NotBlank;

record VehicleCommandDto(@NotBlank VehicleCommand command,
                         @NotBlank String code) {
}
