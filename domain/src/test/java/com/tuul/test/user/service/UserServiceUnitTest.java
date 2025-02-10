package com.tuul.test.user.service;

import com.tuul.test.UnitTest;
import com.tuul.test.auth.model.Token;
import com.tuul.test.auth.service.AuthService;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.model.User;
import com.tuul.test.user.port.FetchUserPort;
import com.tuul.test.user.port.SaveUserPort;
import com.tuul.test.vehicle.model.Vehicle;
import com.tuul.test.vehicle.port.FetchVehiclePort;
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
    private final UUID USER_ID = UUID.randomUUID();
    private final String VALID_TOKEN = "valid-jwt-token";
    private final String INVALID_TOKEN = "invalid-jwt-token";
    private final UUID VEHICLE_ID = UUID.randomUUID();

    private final SaveUserPort saveUserPort = mock(SaveUserPort.class);
    private final FetchUserPort fetchUserPort = mock(FetchUserPort.class);
    private final AuthService authService = mock(AuthService.class);
    private final FetchVehiclePort fetchVehiclePort = mock(FetchVehiclePort.class);
    private final UserService userService = new UserServiceImpl(saveUserPort, fetchUserPort, fetchVehiclePort, authService);

    @Nested
    class when_register_user {

        @Test
        void given_valid_user_then_return_registered_user() {
            var inputUser = User.builder().email(EMAIL).password(PASSWORD).name(NAME).build();
            var registeredUser = inputUser.toBuilder().id(UUID.randomUUID()).build();

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
            var user = User.builder().id(UUID.randomUUID()).email(EMAIL).password(new BCryptPasswordEncoder().encode(PASSWORD)).name(NAME).build();
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
            var user = User.builder().id(UUID.randomUUID()).email(EMAIL).password(new BCryptPasswordEncoder().encode("wrongPassword")).name(NAME).build();

            when(fetchUserPort.findByEmail(EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> userService.authenticateUser(EMAIL, PASSWORD))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Invalid credentials.");
        }
    }

    @Nested
    class when_fetching_details {

        @Test
        void given_valid_token_with_active_vehicle_then_return_user_with_vehicle_details() {
            var user = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .name(NAME)
                    .activeVehicleId(VEHICLE_ID)
                    .build();

            var vehicle = Vehicle.builder()
                    .id(VEHICLE_ID)
                    .code("VEH123")
                    .stateOfCharge(85.0)
                    .build();

            when(authService.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(USER_ID.toString());
            when(fetchUserPort.fetch(USER_ID)).thenReturn(Optional.of(user));
            when(fetchVehiclePort.fetch(VEHICLE_ID)).thenReturn(Optional.of(vehicle));

            var result = userService.fetchDetails(VALID_TOKEN);

            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getActiveVehicle()).isEqualTo(vehicle);

            verify(authService).getUserIdFromJwtToken(VALID_TOKEN);
            verify(fetchUserPort).fetch(USER_ID);
            verify(fetchVehiclePort).fetch(VEHICLE_ID);
        }

        @Test
        void given_valid_token_without_active_vehicle_then_return_user_with_null_vehicle() {
            var user = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .name(NAME)
                    .activeVehicleId(null)
                    .build();

            when(authService.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(USER_ID.toString());
            when(fetchUserPort.fetch(USER_ID)).thenReturn(Optional.of(user));

            var result = userService.fetchDetails(VALID_TOKEN);

            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getActiveVehicle()).isNull();

            verify(authService).getUserIdFromJwtToken(VALID_TOKEN);
            verify(fetchUserPort).fetch(USER_ID);
            verify(fetchVehiclePort, never()).fetch(any());
        }

        @Test
        void given_invalid_token_then_throw_business_violation_exception() {
            when(authService.getUserIdFromJwtToken(INVALID_TOKEN)).thenReturn(USER_ID.toString());
            when(fetchUserPort.fetch(USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.fetchDetails(INVALID_TOKEN))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("User does not exist");

            verify(authService).getUserIdFromJwtToken(INVALID_TOKEN);
            verify(fetchUserPort).fetch(USER_ID);
            verifyNoInteractions(fetchVehiclePort);
        }

        @Test
        void given_valid_token_but_vehicle_not_found_then_return_user_with_null_vehicle() {
            var user = User.builder()
                    .id(USER_ID)
                    .email(EMAIL)
                    .name(NAME)
                    .activeVehicleId(VEHICLE_ID)
                    .build();

            when(authService.getUserIdFromJwtToken(VALID_TOKEN)).thenReturn(USER_ID.toString());
            when(fetchUserPort.fetch(USER_ID)).thenReturn(Optional.of(user));
            when(fetchVehiclePort.fetch(VEHICLE_ID)).thenReturn(Optional.empty());

            var result = userService.fetchDetails(VALID_TOKEN);

            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getActiveVehicle()).isNull();

            verify(authService).getUserIdFromJwtToken(VALID_TOKEN);
            verify(fetchUserPort).fetch(USER_ID);
            verify(fetchVehiclePort).fetch(VEHICLE_ID);
        }
    }
}
