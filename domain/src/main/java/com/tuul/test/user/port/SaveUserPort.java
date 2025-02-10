package com.tuul.test.user.port;

import com.tuul.test.user.model.User;

import java.util.UUID;

public interface SaveUserPort {

    User registerUser(User user);
}
