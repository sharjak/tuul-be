package com.tuul.test.user.controller;

import com.tuul.test.IntegrationTest;
import com.tuul.test.user.service.UserService;
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
}
