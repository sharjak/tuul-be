package com.tuul.test.config;

import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.internal.EmulatorCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;

@Configuration
@Profile("integration-test")
public class FirestoreTestConfig {

    @Value("${firebase.project-id}")
    private String projectId;

    @Value("${firebase.emulator-host}")
    private String emulatorHost;

    @Value("${firebase.emulator-port}")
    private int emulatorPort;

    @Bean
    public Firestore firestore() throws IOException {
        FirestoreOptions firestoreOptions = FirestoreOptions.newBuilder()
                .setEmulatorHost(emulatorHost + ":" + emulatorPort)
                .build();

        FirebaseOptions firebaseOptions = FirebaseOptions.builder()
                .setCredentials(new EmulatorCredentials())
                .setProjectId(projectId)
                .setFirestoreOptions(firestoreOptions)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(firebaseOptions);
        }

        return FirestoreClient.getFirestore();
    }
}
