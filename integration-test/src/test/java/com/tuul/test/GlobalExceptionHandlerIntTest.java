package com.tuul.test;

import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.common.exception.UnexpectedStateException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.assertj.core.api.Assertions.assertThat;

public class GlobalExceptionHandlerIntTest extends IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Nested
    class when_exception_is_thrown {

        @Test
        void given_business_violation_exception_then_return_bad_request() {
            var response = restTemplate.getForEntity("/mock/business", ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("Business rule violated");
            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        void given_method_argument_not_valid_exception_then_return_bad_request() {
            var response = restTemplate.getForEntity("/mock/method-argument-not-valid?code=", ErrorResponse.class);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().status()).isEqualTo(400);
            assertThat(response.getBody().message()).isEqualTo("code: must not be blank");
            assertThat(response.getBody().timestamp()).isNotNull();
        }

        @Test
        void given_unexpected_state_exception_then_return_internal_server_error() {
            var response = restTemplate.getForEntity("/mock/unexpected", ErrorResponse.class);

            assertUnexpectedErrorOccured(response);
        }

        @Test
        void given_generic_exception_then_return_internal_server_error() {
            var response = restTemplate.getForEntity("/mock/generic", ErrorResponse.class);

            assertUnexpectedErrorOccured(response);
        }
    }

    private static void assertUnexpectedErrorOccured(ResponseEntity<ErrorResponse> response) {
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo(500);
        assertThat(response.getBody().message()).isEqualTo("An unexpected error occurred.");
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    record ErrorResponse(int status, String message, String timestamp) {}

    @RestController
    @RequestMapping("/mock")
    static class MockController {

        @GetMapping("/business")
        public void throwBusinessException() {
            throw new BusinessViolationException("Business rule violated");
        }

        @GetMapping("/unexpected")
        public void throwUnexpectedStateException() {
            throw new UnexpectedStateException("Unexpected state occurred");
        }

        @GetMapping("/generic")
        public void throwGenericException() {
            throw new RuntimeException("Generic exception occurred");
        }

        @GetMapping("/method-argument-not-valid")
        public ResponseEntity<Void> throwConstraintViolationException(@Valid SampleDto sampleDto) {
            return ResponseEntity.ok().build();
        }
    }
    record SampleDto(@NotBlank String code) {}
}
