package com.tuul.test.user.util;

import com.tuul.test.common.exception.BusinessViolationException;
import com.tuul.test.user.model.User;
import lombok.experimental.UtilityClass;

import java.util.regex.Pattern;

@UtilityClass
public class UserValidationUtil {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static void validateForRegister(User user) {
        if (user.getId() != null) {
            throw new BusinessViolationException("ID must be null when creating a user");
        }
        validateCommonFields(user);
    }

    private static void validateCommonFields(User user) {
        if (isBlank(user.getName())) {
            throw new BusinessViolationException("Name is required");
        }
        if (isBlank(user.getEmail()) || !EMAIL_PATTERN.matcher(user.getEmail()).matches()) {
            throw new BusinessViolationException("Invalid email format");
        }
        if (isBlank(user.getPassword())) {
            throw new BusinessViolationException("Password is required");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
