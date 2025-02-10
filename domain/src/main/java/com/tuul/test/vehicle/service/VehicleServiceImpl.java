package com.tuul.test.vehicle.service;

import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.vehicle.model.ActiveVehicle;
import com.tuul.test.vehicle.port.FetchVehiclePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final AuthService authService;
    private final FetchVehiclePort fetchVehiclePort;
    private final FetchUserPort fetchUserPort;
    private final SaveUserPort saveUserPort;

    @Override
    public void pair(String token, String code) {
        var userId = UUID.fromString(authService.getUserIdFromJwtToken(token));

        var vehicle = fetchVehiclePort.findByCode(code)
                .orElseThrow(() -> new BusinessViolationException("Vehicle with code not found."));

        if (fetchUserPort.existsActiveVehicleById(userId)) {
            throw new BusinessViolationException("User already paired with a vehicle.");
        }

        if (fetchUserPort.existsActiveVehicleUnderAnyUsers(vehicle.getId())) {
            throw new BusinessViolationException("Vehicle already paired with another user.");
        }

        saveUserPort.saveActiveVehicle(ActiveVehicle.builder()
                .userId(userId)
                .vehicleId(vehicle.getId())
                .build());
    }

    @Override
    public void unpair(String token, String code) {
        var userId = UUID.fromString(authService.getUserIdFromJwtToken(token));

        var vehicle = fetchVehiclePort.findByCode(code)
                .orElseThrow(() -> new BusinessViolationException("Vehicle with code not found."));

        if (!fetchUserPort.existsActiveVehicleUnderUser(userId, vehicle.getId())) {
            throw new BusinessViolationException("User already paired with a vehicle.");
        }

        saveUserPort.deleteActiveVehicle(ActiveVehicle.builder()
                .userId(userId)
                .vehicleId(vehicle.getId())
                .build());
    }
}
