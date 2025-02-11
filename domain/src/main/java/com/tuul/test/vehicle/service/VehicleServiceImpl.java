package com.tuul.test.vehicle.service;

import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.reservation.model.Reservation;
import com.tuul.test.reservation.port.FetchReservationPort;
import com.tuul.test.reservation.port.SaveReservationPort;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.vehicle.model.ActiveVehicle;
import com.tuul.test.vehicle.model.Vehicle;
import com.tuul.test.vehicle.model.VehicleCommand;
import com.tuul.test.vehicle.port.FetchVehiclePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {
    private final AuthService authService;
    private final FetchVehiclePort fetchVehiclePort;
    private final FetchUserPort fetchUserPort;
    private final SaveUserPort saveUserPort;
    private final SaveReservationPort saveReservationPort;
    private final FetchReservationPort fetchReservationPort;
    private final Clock clock;

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
            throw new BusinessViolationException("Vehicle not paired with user.");
        }

        saveUserPort.deleteActiveVehicle(ActiveVehicle.builder()
                .userId(userId)
                .vehicleId(vehicle.getId())
                .build());
    }

    @Override
    public void sendCommand(String token, String code, VehicleCommand command) {
        var userId = UUID.fromString(authService.getUserIdFromJwtToken(token));

        var vehicle = fetchVehiclePort.findByCode(code)
                .orElseThrow(() -> new BusinessViolationException("Vehicle with code not found."));

        if (!fetchUserPort.existsActiveVehicleUnderUser(userId, vehicle.getId())) {
            throw new BusinessViolationException("Vehicle not paired with user.");
        }
        Reservation reservation;
        if (command == VehicleCommand.START) {
            reservation = startReservation(userId, vehicle);
        } else {
            reservation = endReservation(userId, vehicle);
        }
        saveReservationPort.save(reservation);
    }

    private Reservation startReservation(UUID userId, Vehicle vehicle) {
        if (fetchReservationPort.existsActiveReservationForUserOrVehicle(userId, vehicle.getId())) {
            throw new BusinessViolationException("Vehicle or user already has active reservation.");
        }
        return Reservation.builder()
                .id(UUID.randomUUID())
                .vehicleId(vehicle.getId())
                .userId(userId)
                .startTime(LocalDateTime.now(clock))
                .startingLocation(vehicle.getCoordinates())
                .build();
    }

    private Reservation endReservation(UUID userId, Vehicle vehicle) {
        var activeReservation = fetchReservationPort.fetchActiveReservation(userId, vehicle.getId())
                .orElseThrow(() -> new BusinessViolationException("Active reservation not found."));
        var endTime = LocalDateTime.now(clock);
        return activeReservation.toBuilder()
                .endTime(LocalDateTime.now(clock))
                .endingLocation(vehicle.getCoordinates())
                .costOfReservation(calculateCost(activeReservation.getStartTime(), endTime))
                .build();
    }

    private BigDecimal calculateCost(LocalDateTime startTime, LocalDateTime endTime) {
        var totalMinutes = Duration.between(startTime, endTime).toMinutes();

        var baseFee = BigDecimal.valueOf(1.0);
        var firstTenMinuteRate = BigDecimal.valueOf(0.5);
        var additionalMinuteRate = BigDecimal.valueOf(0.3);

        var firstTenMinutes = Math.min(totalMinutes, 10);
        var extraMinutes = Math.max(0, totalMinutes - 10);

        var firstPartCost = firstTenMinuteRate.multiply(BigDecimal.valueOf(firstTenMinutes));
        var extraPartCost = additionalMinuteRate.multiply(BigDecimal.valueOf(extraMinutes));

        return baseFee.add(firstPartCost).add(extraPartCost).setScale(2, RoundingMode.HALF_UP);
    }
}
