package com.tuul.test.user.service;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;

public interface UserService {

    User registerUser(User user);

    Token authenticateUser(String email, String password);

}
