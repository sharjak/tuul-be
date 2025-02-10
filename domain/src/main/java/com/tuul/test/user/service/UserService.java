package com.tuul.test.user.service;

import com.tuul.test.auth.model.Token;
import com.tuul.test.user.model.User;
import com.tuul.test.user.model.UserWithDetails;

public interface UserService {

    User registerUser(User user);

    Token authenticateUser(String email, String password);

    UserWithDetails fetchDetails(String token);

}
