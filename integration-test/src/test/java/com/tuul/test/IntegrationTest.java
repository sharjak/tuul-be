package com.tuul.test;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.StreamSupport;

@ActiveProfiles("integration-test")
@SpringBootTest(classes = TuulScootersApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public abstract class IntegrationTest {

    @Autowired
    private Firestore firestore;

    @BeforeEach
    public void setupFirestore() {
        clearFirestore();
        seedVehiclesCollection();
    }

    private void clearFirestore() {
        StreamSupport.stream(firestore.listCollections().spliterator(), false)
                .forEach(this::deleteCollection);
    }

    private void deleteCollection(CollectionReference collection) {
        try {
            for (var document : collection.get().get().getDocuments()) {
                document.getReference().delete().get();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to clean Firestore collection: " + collection.getId(), e);
        }
    }

    private void seedVehiclesCollection() {
        try {
            var resource = new ClassPathResource("vehicles.json");
            var reader = new InputStreamReader(resource.getInputStream());

            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, Object>>>() {}.getType();
            List<Map<String, Object>> vehicles = gson.fromJson(reader, listType);

            CollectionReference vehicleCollection = firestore.collection("vehicles");

            for (Map<String, Object> vehicle : vehicles) {
                String id = (String) vehicle.get("id");
                DocumentReference docRef = vehicleCollection.document(id);
                ApiFuture<?> future = docRef.set(vehicle);
                future.get();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed vehicles collection", e);
        }
    }
}
