package com.tuul.test.reservation.model;

import com.tuul.test.common.model.Coordinates;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Reservation {
    private UUID id;
    private UUID vehicleId;
    private UUID userId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Coordinates startingLocation;
    private Coordinates endingLocation;
    private BigDecimal costOfReservation;
}
