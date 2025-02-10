package com.tuul.test.user.port;

import com.tuul.test.user.model.User;

import java.util.Optional;

public interface FetchUserPort {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
