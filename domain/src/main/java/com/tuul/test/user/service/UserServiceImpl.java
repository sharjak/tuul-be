package com.tuul.test.user.service;

import com.tuul.test.auth.model.Token;
import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.model.User;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.user.util.UserValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final SaveUserPort saveUserPort;
    private final FetchUserPort fetchUserPort;
    private final AuthService authService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User registerUser(User user) {
        UserValidationUtil.validateForRegister(user);
        validateUserDoesNotExistByEmail(user);
        user = hashUserPassword(user);
        return saveUserPort.registerUser(user);
    }

    @Override
    public Token authenticateUser(String email, String password) {
        User user = fetchUserPort.findByEmail(email)
                .orElseThrow(() -> new BusinessViolationException("User with email does not exist."));
        validateUserPasswordMatches(user.getPassword(), password);
        return authService.generateJwtToken(user);
    }

    private User hashUserPassword(User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        return user.toBuilder().password(hashedPassword).build();
    }

    private void validateUserDoesNotExistByEmail(User user) {
        if (fetchUserPort.existsByEmail(user.getEmail())) {
            throw new BusinessViolationException("User with this email already exists.");
        }
    }

    private void validateUserPasswordMatches(String passwordInDb, String userEnteredPassword) {
        if (!passwordEncoder.matches(userEnteredPassword, passwordInDb)) {
            throw new BusinessViolationException("Invalid credentials.");
        }
    }
}

