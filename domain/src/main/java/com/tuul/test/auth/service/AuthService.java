package com.tuul.test.auth.service;

import com.tuul.test.user.model.User;

public interface AuthService {

    String generateJwtToken(User user);

    boolean validateJwtToken(String token);

    String getUserIdFromJwtToken(String token);
}
