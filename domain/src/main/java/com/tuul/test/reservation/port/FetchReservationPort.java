package com.tuul.test.reservation.port;

import com.tuul.test.reservation.model.Reservation;

import java.util.Optional;
import java.util.UUID;

public interface FetchReservationPort {
    boolean existsActiveReservationForUserOrVehicle(UUID user, UUID vehicle);
    Optional<Reservation> fetchActiveReservation(UUID user, UUID vehicle);
}
