package com.tuul.test.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.tuul.test.util.FirestoreUtils;
import com.tuul.test.user.model.User;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.vehicle.model.ActiveVehicle;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class SaveUserRepository implements SaveUserPort {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "users";
    private static final String VEHICLES_COLLECTION_NAME = "vehicles";

    @Override
    public User registerUser(User user) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var userId = UUID.randomUUID();
            user.setId(userId);

            Map<String, Object> userData = Map.of(
                    "id", userId.toString(),
                    "email", user.getEmail(),
                    "password", user.getPassword(),
                    "name", user.getName()
            );

            firestore.collection(COLLECTION_NAME)
                    .document(userId.toString())
                    .set(userData)
                    .get();

            return user;
        }, "Failed to register user in Firestore");
    }

    @Override
    public void saveActiveVehicle(ActiveVehicle activeVehicle) {
        FirestoreUtils.safeFirestoreQuery(() -> {
            DocumentReference userRef = firestore.collection(COLLECTION_NAME)
                    .document(activeVehicle.getUserId().toString());

            DocumentReference vehicleRef = firestore.collection(VEHICLES_COLLECTION_NAME)
                    .document(activeVehicle.getVehicleId().toString());

            ApiFuture<WriteResult> future = userRef.update("activeVehicle", vehicleRef);
            future.get();
            return null;
        }, "Failed to save active vehicle under user");
    }

    @Override
    public void deleteActiveVehicle(ActiveVehicle activeVehicle) {
        FirestoreUtils.safeFirestoreQuery(() -> {
            DocumentReference userRef = firestore.collection(COLLECTION_NAME)
                    .document(activeVehicle.getUserId().toString());

            ApiFuture<WriteResult> future = userRef.update("activeVehicle", FieldValue.delete());
            future.get();
            return null;
        }, "Failed to delete active vehicle under user");
    }
}
