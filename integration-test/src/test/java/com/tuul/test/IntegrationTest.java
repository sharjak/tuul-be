package com.tuul.test;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
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
    public void clearFirestore() {
        // Convert Iterable to Stream
        StreamSupport.stream(firestore.listCollections().spliterator(), false)
                .forEach(this::deleteCollection);
    }

    private void deleteCollection(CollectionReference collection) {
        try {
            ApiFuture<QuerySnapshot> future = collection.get();
            for (QueryDocumentSnapshot document : future.get().getDocuments()) {
                document.getReference().delete().get();
            }
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("Failed to clean Firestore collection: " + collection.getId(), e);
        }
    }
}
