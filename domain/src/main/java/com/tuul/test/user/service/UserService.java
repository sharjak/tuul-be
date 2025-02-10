package com.tuul.test.user.service;

import com.tuul.test.user.model.User;

public interface UserService {

    User registerUser(User user);

    String authenticateUser(String email, String password);

}
