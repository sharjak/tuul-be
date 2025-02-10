package com.tuul.test.vehicle.controller;

import com.tuul.test.IntegrationTest;
import com.tuul.test.user.model.User;
import com.tuul.test.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import static org.assertj.core.api.Assertions.assertThat;

public class VehicleControllerIntTest extends IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private static final String NAME = "John Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String PASSWORD = "securePassword123";
    private static final String VEHICLE_CODE = "code1";

    private String token;

    @BeforeEach
    void setup() {
        var user = User.builder()
                .name(NAME)
                .email(EMAIL)
                .password(PASSWORD)
                .build();
        userService.registerUser(user);

        var authToken = userService.authenticateUser(EMAIL, PASSWORD);
        token = authToken.getToken();
    }

    @Nested
    class when_pair_vehicle {

        @Test
        void given_valid_token_and_code_then_pair_vehicle_successfully() {
            var pairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity("/vehicle/pair", request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void given_invalid_token_then_return_unauthorized() {
            var pairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer invalidToken");
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity("/vehicle/pair", request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void given_non_existent_vehicle_code_then_return_bad_request() {
            var pairRequest = new PairVehicleDto("invalidCode");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, headers);

            ResponseEntity<String> response = restTemplate.postForEntity("/vehicle/pair", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class when_unpair_vehicle {

        @BeforeEach
        void pairVehicle() {
            var pairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, headers);

            restTemplate.postForEntity("/vehicle/pair", request, Void.class);
        }

        @Test
        void given_valid_token_and_code_then_unpair_vehicle_successfully() {
            var unpairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(unpairRequest, headers);

            ResponseEntity<Void> response = restTemplate.exchange("/vehicle/pair", HttpMethod.DELETE, request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void given_invalid_token_then_return_unauthorized() {
            var unpairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer invalidToken");
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(unpairRequest, headers);

            ResponseEntity<Void> response = restTemplate.exchange("/vehicle/pair", HttpMethod.DELETE, request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        }

        @Test
        void given_non_paired_vehicle_then_return_bad_request() {
            var unpairRequest = new PairVehicleDto("invalidCode");

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(unpairRequest, headers);

            ResponseEntity<String> response = restTemplate.exchange("/vehicle/pair", HttpMethod.DELETE, request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}

