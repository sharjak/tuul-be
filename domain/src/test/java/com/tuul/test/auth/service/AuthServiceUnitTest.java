package com.tuul.test.auth.service;

import com.tuul.test.UnitTest;
import com.tuul.test.user.model.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceUnitTest extends UnitTest {

    private static final String SECRET_KEY = "S+QXTyzSAuY+rmTl05TCewS5F9UngYoYFvZ3UrR76yFZ+HXsuN1QJmaIDOw/Nx/yTWUuLOiy/yV/FQhsVLK36A==";
    private static final long EXPIRATION_MS = 3600000;

    private final Clock fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private final AuthService authService = new AuthServiceImpl(fixedClock, SECRET_KEY, EXPIRATION_MS);

    @Nested
    class when_generate_jwt_token {

        @Test
        void given_valid_user_then_return_jwt_token() {
            var user = User.builder()
                    .id(UUID.randomUUID())
                    .email("test@example.com")
                    .build();

            var token = authService.generateJwtToken(user);

            assertThat(token).isNotNull();
        }
    }

    @Nested
    class when_validate_jwt_token {

        @Test
        void given_valid_token_then_return_true() {
            var user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
            var token = authService.generateJwtToken(user);

            var isValid = authService.validateJwtToken(token.getToken());

            assertThat(isValid).isTrue();
        }

        @Test
        void given_expired_token_then_return_false() {
            var expiredAuthService = new AuthServiceImpl(fixedClock, SECRET_KEY, -1000L);
            var user = User.builder().id(UUID.randomUUID()).email("test@example.com").build();
            var expiredToken = expiredAuthService.generateJwtToken(user);

            var isValid = expiredAuthService.validateJwtToken(expiredToken.getToken());

            assertThat(isValid).isFalse();
        }
    }

    @Nested
    class when_extracting_user_id_from_token {

        @Test
        void given_valid_token_then_return_user_id() {
            var userId = UUID.randomUUID();
            var user = User.builder().id(userId).email("test@example.com").build();
            var token = authService.generateJwtToken(user);

            String extractedUserId = authService.getUserIdFromJwtToken(token.getToken());

            assertThat(UUID.fromString(extractedUserId)).isEqualTo(userId);
        }
    }
}
