package com.tuul.test.util;

import com.tuul.test.common.exception.DatabaseAccessException;
import lombok.experimental.UtilityClass;

import java.util.concurrent.ExecutionException;

@UtilityClass
public class FirestoreUtils {

    public static <T> T safeFirestoreQuery(FirestoreQuery<T> query, String errorMessage) {
        try {
            return query.execute();
        } catch (ExecutionException | InterruptedException e) {
            throw new DatabaseAccessException(errorMessage, e);
        }
    }
}
