package com.tuul.test.reservation;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.tuul.test.reservation.model.Reservation;
import com.tuul.test.reservation.port.SaveReservationPort;
import com.tuul.test.util.FirestoreUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@RequiredArgsConstructor
class SaveReservationRepository implements SaveReservationPort {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "reservations";

    @Override
    public Reservation save(Reservation reservation) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            DocumentReference reservationRef = firestore.collection(COLLECTION_NAME)
                    .document(reservation.getId().toString());

            Map<String, Object> reservationData = new HashMap<>();
            reservationData.put("id", reservation.getId().toString());
            reservationData.put("user", firestore.collection("users").document(reservation.getUserId().toString()));
            reservationData.put("vehicle", firestore.collection("vehicles").document(reservation.getVehicleId().toString()));
            reservationData.put("startTime", reservation.getStartTime().toString());
            reservationData.put("startingLocation", reservation.getStartingLocation());

            if (reservation.getEndTime() != null) {
                reservationData.put("endTime", reservation.getEndTime().toString());
            }
            if (reservation.getEndingLocation() != null) {
                reservationData.put("endingLocation", reservation.getEndingLocation());
            }
            if (reservation.getCostOfReservation() != null) {
                reservationData.put("costOfReservation", reservation.getCostOfReservation().toString());
            }

            reservationRef.set(reservationData).get();

            return reservation;
        }, "Failed to save reservation in Firestore");
    }
}
