package com.tuul.test.user;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.tuul.test.common.exception.UnexpectedStateException;
import com.tuul.test.user.model.User;
import com.tuul.test.user.port.SaveUserPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Repository
@RequiredArgsConstructor
public class SaveUserRepository implements SaveUserPort {
    private final Firestore firestore;
    private static final String COLLECTION_NAME = "users";

    @Override
    public User registerUser(User user) {
        try {
            String userId = UUID.randomUUID().toString();
            user.setId(userId);

            CollectionReference users = firestore.collection(COLLECTION_NAME);
            ApiFuture<WriteResult> future = users.document(userId).set(user);
            future.get();

            return user;
        } catch (ExecutionException | InterruptedException e) {
            throw new UnexpectedStateException("Failed to register user in Firestore: " + e.getMessage());
        }
    }
}

