package com.tuul.test.reservation;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.tuul.test.common.model.Coordinates;
import com.tuul.test.reservation.model.Reservation;
import com.tuul.test.reservation.port.FetchReservationPort;
import com.tuul.test.util.FirestoreUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class FetchReservationRepository implements FetchReservationPort {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "reservations";

    @Override
    public boolean existsActiveReservationForUserOrVehicle(UUID userId, UUID vehicleId) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var userRef = firestore.document("users/" + userId.toString());
            var vehicleRef = firestore.document("vehicles/" + vehicleId.toString());

            var userQuerySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user", userRef)
                    .get()
                    .get();

            var vehicleQuerySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("vehicle", vehicleRef)
                    .get()
                    .get();

            var activeUserReservations = userQuerySnapshot.getDocuments().stream()
                    .filter(doc -> !doc.getData().containsKey("endTime"))
                    .toList();

            var activeVehicleReservations = vehicleQuerySnapshot.getDocuments().stream()
                    .filter(doc -> !doc.getData().containsKey("endTime"))
                    .toList();

            return !activeUserReservations.isEmpty() || !activeVehicleReservations.isEmpty();
        }, "Failed to check active reservation in Firestore");
    }

    @Override
    public Optional<Reservation> fetchActiveReservation(UUID userId, UUID vehicleId) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var userRef = firestore.document("users/" + userId.toString());
            var vehicleRef = firestore.document("vehicles/" + vehicleId.toString());

            var reservationQuerySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("user", userRef)
                    .whereEqualTo("vehicle", vehicleRef)
                    .get()
                    .get();

            var activeReservations = reservationQuerySnapshot.getDocuments().stream()
                    .filter(doc -> !doc.getData().containsKey("endTime"))
                    .toList();

            if (activeReservations.size() > 1) {
                throw new IllegalStateException("Multiple active reservations found.");
            }

            if (activeReservations.isEmpty()) {
                return Optional.empty();
            }

            var document = activeReservations.get(0);
            var reservation = new Reservation();
            reservation.setId(UUID.fromString(document.getId()));

            DocumentReference fetchedUserRef = document.get("user", DocumentReference.class);
            DocumentReference fetchedVehicleRef = document.get("vehicle", DocumentReference.class);

            if (fetchedUserRef != null) {
                reservation.setUserId(UUID.fromString(fetchedUserRef.getId()));
            }

            if (fetchedVehicleRef != null) {
                reservation.setVehicleId(UUID.fromString(fetchedVehicleRef.getId()));
            }

            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

            String startTimeStr = document.getString("startTime");
            if (startTimeStr != null) {
                reservation.setStartTime(LocalDateTime.parse(startTimeStr, formatter));
            }

            String endTimeStr = document.getString("endTime");
            if (endTimeStr != null) {
                reservation.setEndTime(LocalDateTime.parse(endTimeStr, formatter));
            }

            reservation.setStartingLocation(document.get("startingLocation", Coordinates.class));
            reservation.setEndingLocation(document.get("endingLocation", Coordinates.class));

            String costStr = document.getString("costOfReservation");
            if (costStr != null) {
                reservation.setCostOfReservation(new BigDecimal(costStr));
            }

            return Optional.of(reservation);
        }, "Failed to fetch active reservation in Firestore");
    }


}
