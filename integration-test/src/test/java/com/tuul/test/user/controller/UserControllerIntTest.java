package com.tuul.test.user.controller;

import com.tuul.test.IntegrationTest;
import com.tuul.test.user.service.UserService;
import com.tuul.test.vehicle.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class UserControllerIntTest extends IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    @Autowired
    private VehicleService vehicleService;

    private static final String NAME = "John Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "securePassword123";

    @BeforeEach
    void setup() {
        var request = new SaveUserDto(NAME, EMAIL, PASSWORD);
        restTemplate.postForEntity("/user", request, UserDto.class);
    }

    @Nested
    class when_register_user {

        @Test
        void given_valid_user_then_creates_and_returns_user_with_id() {
            var request = new SaveUserDto("Jane Doe", "jane.doe@example.com", "password123");

            ResponseEntity<UserDto> response = restTemplate.postForEntity("/user", request, UserDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().id()).isNotNull();
            assertThat(response.getBody().name()).isEqualTo("Jane Doe");
            assertThat(response.getBody().email()).isEqualTo("jane.doe@example.com");
        }

        @Test
        void given_invalid_email_then_returns_bad_request() {
            var request = new SaveUserDto(NAME, "invalid-email", PASSWORD);

            ResponseEntity<String> response = restTemplate.postForEntity("/user", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void given_missing_password_then_returns_bad_request() {
            var request = new SaveUserDto("Jane Doe", "jane.doe@example.com", null);

            ResponseEntity<String> response = restTemplate.postForEntity("/user", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class when_login_user {

        @Test
        void given_valid_credentials_then_return_jwt_token() {
            var request = new LoginUserDto(EMAIL, PASSWORD);

            ResponseEntity<TokenDto> response = restTemplate.postForEntity("/user/login", request, TokenDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().token()).startsWith("ey");
        }

        @Test
        void given_invalid_password_then_return_bad_request() {
            var request = new LoginUserDto(EMAIL, "wrongPassword");

            ResponseEntity<String> response = restTemplate.postForEntity("/user/login", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void given_non_existent_email_then_return_bad_request() {
            var request = new LoginUserDto("nonexistent@example.com", PASSWORD);

            ResponseEntity<String> response = restTemplate.postForEntity("/user/login", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class when_fetching_user_details {
        private String token;

        @BeforeEach
        void authenticateUser() {
            var loginRequest = new LoginUserDto(EMAIL, PASSWORD);
            ResponseEntity<TokenDto> loginResponse = restTemplate.postForEntity("/user/login", loginRequest, TokenDto.class);
            token = loginResponse.getBody().token();
        }

        @Test
        void given_valid_token_with_active_vehicle_then_return_user_details() {
            vehicleService.pair(token, "code1");

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<UserDetailsDto> response = restTemplate.exchange("/user/details", HttpMethod.GET, entity, UserDetailsDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().email()).isEqualTo(EMAIL);
            assertThat(response.getBody().activeVehicle()).isNotNull();
            assertThat(response.getBody().activeVehicle().code()).isEqualTo("code1");
        }

        @Test
        void given_valid_token_without_active_vehicle_then_return_user_details_with_null_vehicle() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<UserDetailsDto> response = restTemplate.exchange("/user/details", HttpMethod.GET, entity, UserDetailsDto.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().email()).isEqualTo(EMAIL);
            assertThat(response.getBody().activeVehicle()).isNull();
        }

        @Test
        void given_invalid_token_then_return_unauthorized() {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth("invalid-token");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange("/user/details", HttpMethod.GET, entity, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void given_missing_token_then_return_unauthorized() {
            ResponseEntity<String> response = restTemplate.getForEntity("/user/details", String.class);

            assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED);
        }
    }

}
