package com.tuul.test.reservation.port;

import com.tuul.test.reservation.model.Reservation;

public interface SaveReservationPort {
    Reservation save(Reservation reservation);
}
