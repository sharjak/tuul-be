package com.tuul.test.util;

import java.util.concurrent.ExecutionException;

@FunctionalInterface
public interface FirestoreQuery<T> {
    T execute() throws ExecutionException, InterruptedException;
}
