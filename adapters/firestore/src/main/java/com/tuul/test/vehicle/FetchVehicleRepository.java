package com.tuul.test.vehicle;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.tuul.test.util.FirestoreUtils;
import com.tuul.test.vehicle.model.Vehicle;
import com.tuul.test.vehicle.port.FetchVehiclePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class FetchVehicleRepository implements FetchVehiclePort {
    private static final String COLLECTION_NAME = "vehicles";
    private final Firestore firestore;

    @Override
    public Optional<Vehicle> findByCode(String code) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("vehicleCode", code)
                    .get()
                    .get();

            if (querySnapshot.isEmpty()) {
                return Optional.empty();
            }

            QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);

            Vehicle vehicle = Vehicle.builder()
                    .id(UUID.fromString(document.getId()))
                    .code(document.getString("vehicleCode"))
                    .stateOfCharge(document.getDouble("stateOfCharge"))
                    .build();

            return Optional.of(vehicle);
        }, "Error fetching vehicle from Firestore");
    }
}
