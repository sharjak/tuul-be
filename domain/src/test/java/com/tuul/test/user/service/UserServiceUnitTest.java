package com.tuul.test.user.service;

import com.tuul.test.UnitTest;
import com.tuul.test.auth.model.Token;
import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.model.User;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserServiceUnitTest extends UnitTest {

    private static final String EMAIL = "john@example.com";
    private static final String INVALID_EMAIL = "invalid-email";
    private static final String PASSWORD = "securePassword";
    private static final String NAME = "John Doe";

    private final SaveUserPort saveUserPort = mock(SaveUserPort.class);
    private final FetchUserPort fetchUserPort = mock(FetchUserPort.class);
    private final AuthService authService = mock(AuthService.class);
    private final UserService userService = new UserServiceImpl(saveUserPort, fetchUserPort, authService);

    @Nested
    class when_register_user {

        @Test
        void given_valid_user_then_return_registered_user() {
            var inputUser = User.builder().email(EMAIL).password(PASSWORD).name(NAME).build();
            var registeredUser = inputUser.toBuilder().id(UUID.randomUUID().toString()).build();

            when(fetchUserPort.existsByEmail(EMAIL)).thenReturn(false);
            when(saveUserPort.registerUser(any(User.class))).thenReturn(registeredUser);

            var result = userService.registerUser(inputUser);

            assertThat(result).isEqualTo(registeredUser);
            verify(fetchUserPort).existsByEmail(EMAIL);
            verify(saveUserPort).registerUser(any(User.class));
        }

        @Test
        void given_existing_email_then_throw_business_violation_exception() {
            var inputUser = User.builder().email(EMAIL).password(PASSWORD).name(NAME).build();

            when(fetchUserPort.existsByEmail(EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> userService.registerUser(inputUser))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("User with this email already exists.");

            verify(fetchUserPort).existsByEmail(EMAIL);
            verifyNoInteractions(saveUserPort);
        }

        @Test
        void given_invalid_email_then_throw_business_violation_exception() {
            var invalidUser = User.builder().email(INVALID_EMAIL).password(PASSWORD).name(NAME).build();

            assertThatThrownBy(() -> userService.registerUser(invalidUser))
                    .isInstanceOf(BusinessViolationException.class);
        }
    }

    @Nested
    class when_authenticate_user {

        @Test
        void given_valid_credentials_then_return_jwt_token() {
            var user = User.builder().id(UUID.randomUUID().toString()).email(EMAIL).password(new BCryptPasswordEncoder().encode(PASSWORD)).name(NAME).build();
            var token = Token.builder().token("dummy-jwt-token").build();

            when(fetchUserPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));
            when(authService.generateJwtToken(user)).thenReturn(token);

            var result = userService.authenticateUser(EMAIL, PASSWORD);

            assertThat(result).isEqualTo(token);
            verify(fetchUserPort).findByEmail(EMAIL);
            verify(authService).generateJwtToken(user);
        }

        @Test
        void given_invalid_email_then_throw_business_violation_exception() {
            when(fetchUserPort.findByEmail(EMAIL)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.authenticateUser(EMAIL, PASSWORD))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("User with email does not exist.");
        }

        @Test
        void given_invalid_password_then_throw_business_violation_exception() {
            var user = User.builder().id(UUID.randomUUID().toString()).email(EMAIL).password(new BCryptPasswordEncoder().encode("wrongPassword")).name(NAME).build();

            when(fetchUserPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.authenticateUser(EMAIL, PASSWORD))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Invalid credentials.");
        }
    }
}
