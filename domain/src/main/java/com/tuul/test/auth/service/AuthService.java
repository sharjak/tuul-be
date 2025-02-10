package com.tuul.test.auth.service;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;

public interface AuthService {

    Token generateJwtToken(User user);

    boolean validateJwtToken(String token);

    String getUserIdFromJwtToken(String token);
}
