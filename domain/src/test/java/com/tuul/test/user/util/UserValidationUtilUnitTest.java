package com.tuul.test.user.util;

import com.tuul.test.UnitTest;
import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.model.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserValidationUtilUnitTest extends UnitTest {
    private static final String NAME = "John Doe";
    private static final String EMAIL = "john@example.com";
    private static final String PASSWORD = "securePassword";

    @Nested
    class when_validate_for_register {

        @Test
        void given_valid_user_then_no_exception() {
            var user = User.builder().name(NAME).email(EMAIL).password(PASSWORD).build();
            UserValidationUtil.validateForRegister(user);
        }

        @Test
        void given_user_with_non_null_id_then_throw_exception() {
            var user = User.builder().id("12345").name(NAME).email(EMAIL).password(PASSWORD).build();

            assertThatThrownBy(() -> UserValidationUtil.validateForRegister(user))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("ID must be null when creating a user");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        void given_user_with_blank_or_null_name_then_throw_exception(String blankName) {
            var user = User.builder().name(blankName).email(EMAIL).password(PASSWORD).build();

            assertThatThrownBy(() -> UserValidationUtil.validateForRegister(user))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Name is required");
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        void given_user_with_blank_or_null_email_then_throw_exception(String blankEmail) {
            var user = User.builder().name(NAME).email(blankEmail).password(PASSWORD).build();

            assertThatThrownBy(() -> UserValidationUtil.validateForRegister(user))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Invalid email format");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "john@.com", "john@com", "@example.com", "john@example"})
        void given_user_with_invalid_email_then_throw_exception(String invalidEmail) {
            var user = User.builder().name(NAME).email(invalidEmail).password(PASSWORD).build();

            assertThatThrownBy(() -> UserValidationUtil.validateForRegister(user))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Invalid email format");
        }

        @ParameterizedTest
        @ValueSource(strings = {"john.doe@example.com", "user123@mail.com", "valid_email@sub.example.co.uk"})
        void given_user_with_valid_email_then_no_exception(String validEmail) {
            var user = User.builder().name(NAME).email(validEmail).password(PASSWORD).build();
            UserValidationUtil.validateForRegister(user);
        }

        @ParameterizedTest
        @NullSource
        @ValueSource(strings = {"", " ", "   "})
        void given_user_with_blank_or_null_password_then_throw_exception(String blankPassword) {
            var user = User.builder().name(NAME).email(EMAIL).password(blankPassword).build();

            assertThatThrownBy(() -> UserValidationUtil.validateForRegister(user))
                    .isInstanceOf(BusinessViolationException.class)
                    .hasMessage("Password is required");
        }
    }
}
