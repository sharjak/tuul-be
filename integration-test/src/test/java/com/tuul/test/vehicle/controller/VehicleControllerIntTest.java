package com.tuul.test.vehicle.controller;

import com.tuul.test.IntegrationTest;
import com.tuul.test.user.model.User;
import com.tuul.test.user.service.UserService;
import com.tuul.test.vehicle.model.VehicleCommand;
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
    private static final HttpHeaders AUTH_HEADERS = new HttpHeaders();

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
        AUTH_HEADERS.set("Authorization", "Bearer " + token);
    }

    @Nested
    class when_pair_vehicle {

        @Test
        void given_valid_token_and_code_then_pair_vehicle_successfully() {
            var pairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, AUTH_HEADERS);

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

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void given_non_existent_vehicle_code_then_return_bad_request() {
            var pairRequest = new PairVehicleDto("invalidCode");

            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, AUTH_HEADERS);

            ResponseEntity<String> response = restTemplate.postForEntity("/vehicle/pair", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class when_unpair_vehicle {

        @BeforeEach
        void pairVehicle() {
            var pairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, AUTH_HEADERS);

            restTemplate.postForEntity("/vehicle/pair", request, Void.class);
        }

        @Test
        void given_valid_token_and_code_then_unpair_vehicle_successfully() {
            var unpairRequest = new PairVehicleDto(VEHICLE_CODE);

            HttpEntity<PairVehicleDto> request = new HttpEntity<>(unpairRequest, AUTH_HEADERS);

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

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void given_non_paired_vehicle_then_return_bad_request() {
            var unpairRequest = new PairVehicleDto("invalidCode");

            HttpEntity<PairVehicleDto> request = new HttpEntity<>(unpairRequest, AUTH_HEADERS);

            ResponseEntity<String> response = restTemplate.exchange("/vehicle/pair", HttpMethod.DELETE, request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    class when_send_command {

        @BeforeEach
        void pairVehicle() {
            var pairRequest = new PairVehicleDto(VEHICLE_CODE);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(pairRequest, AUTH_HEADERS);
            restTemplate.postForEntity("/vehicle/pair", request, Void.class);
        }

        @Test
        void given_valid_start_command_then_start_reservation_successfully() {
            var commandRequest = new VehicleCommandDto(VehicleCommand.START, VEHICLE_CODE);
            HttpEntity<VehicleCommandDto> request = new HttpEntity<>(commandRequest, AUTH_HEADERS);

            ResponseEntity<Void> response = restTemplate.postForEntity("/vehicle/command", request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void given_valid_stop_command_then_end_reservation_successfully() {
            var startCommand = new VehicleCommandDto(VehicleCommand.START, VEHICLE_CODE);
            HttpEntity<VehicleCommandDto> startRequest = new HttpEntity<>(startCommand, AUTH_HEADERS);
            restTemplate.postForEntity("/vehicle/command", startRequest, Void.class);

            var stopCommand = new VehicleCommandDto(VehicleCommand.STOP, VEHICLE_CODE);
            HttpEntity<VehicleCommandDto> stopRequest = new HttpEntity<>(stopCommand, AUTH_HEADERS);

            ResponseEntity<Void> response = restTemplate.postForEntity("/vehicle/command", stopRequest, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        void given_invalid_token_then_return_unauthorized() {
            var commandRequest = new VehicleCommandDto(VehicleCommand.START, VEHICLE_CODE);

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer invalidToken");
            HttpEntity<VehicleCommandDto> request = new HttpEntity<>(commandRequest, headers);

            ResponseEntity<Void> response = restTemplate.postForEntity("/vehicle/command", request, Void.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        void given_unpaired_vehicle_then_return_bad_request() {
            var unpairRequest = new PairVehicleDto(VEHICLE_CODE);
            HttpEntity<PairVehicleDto> request = new HttpEntity<>(unpairRequest, AUTH_HEADERS);
            restTemplate.exchange("/vehicle/pair", HttpMethod.DELETE, request, Void.class);

            var commandRequest = new VehicleCommandDto(VehicleCommand.START, VEHICLE_CODE);
            HttpEntity<VehicleCommandDto> commandHttpRequest = new HttpEntity<>(commandRequest, AUTH_HEADERS);

            ResponseEntity<String> response = restTemplate.postForEntity("/vehicle/command", commandHttpRequest, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        void given_invalid_vehicle_code_then_return_bad_request() {
            var commandRequest = new VehicleCommandDto(VehicleCommand.START, "invalidCode");

            HttpEntity<VehicleCommandDto> request = new HttpEntity<>(commandRequest, AUTH_HEADERS);

            ResponseEntity<String> response = restTemplate.postForEntity("/vehicle/command", request, String.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }
}

