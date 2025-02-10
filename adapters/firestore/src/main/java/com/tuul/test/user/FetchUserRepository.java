package com.tuul.test.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.tuul.test.util.FirestoreUtils;
import com.tuul.test.user.model.User;
import com.tuul.test.user.port.FetchUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FetchUserRepository implements FetchUserPort {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "users";

    @Override
    public Optional<User> findByEmail(String email) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            ApiFuture<QuerySnapshot> future = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .limit(1)
                    .get();

            QuerySnapshot querySnapshot = future.get();

            if (!querySnapshot.isEmpty()) {
                QueryDocumentSnapshot document = querySnapshot.getDocuments().get(0);
                User user = document.toObject(User.class);
                user.setId(document.getId());
                return Optional.of(user);
            }

            return Optional.empty();
        }, "Failed to fetch user by email.");
    }

    @Override
    public boolean existsByEmail(String email) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            QuerySnapshot querySnapshot = firestore.collection(COLLECTION_NAME)
                    .whereEqualTo("email", email)
                    .get()
                    .get();

            return !querySnapshot.isEmpty();
        }, "Failed to check if user exists by email");
    }

    @Override
    public boolean existsActiveVehicleById(UUID userId) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var querySnapshot = firestore.collection("users")
                    .whereEqualTo("id", userId.toString())
                    .whereNotEqualTo("activeVehicle", null)
                    .limit(1)
                    .get()
                    .get();

            return !querySnapshot.isEmpty();
        }, "Error checking active vehicle by user ID");
    }

    @Override
    public boolean existsActiveVehicleUnderAnyUsers(UUID vehicleId) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var querySnapshot = firestore.collection("users")
                    .whereEqualTo("activeVehicle", firestore.document("vehicles/" + vehicleId.toString()))
                    .limit(1)
                    .get()
                    .get();

            return !querySnapshot.isEmpty();
        }, "Error checking active vehicle under any users");
    }

    @Override
    public boolean existsActiveVehicleUnderUser(UUID userId, UUID vehicleId) {
        return FirestoreUtils.safeFirestoreQuery(() -> {
            var querySnapshot = firestore.collection("users")
                    .whereEqualTo("id", userId.toString())
                    .whereEqualTo("activeVehicle", firestore.document("vehicles/" + vehicleId.toString()))
                    .limit(1)
                    .get()
                    .get();

            return !querySnapshot.isEmpty();
        }, "Error checking active vehicle under user");
    }
}
