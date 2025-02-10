package com.tuul.test.user.controller;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

record LoginUserDto(@Email String email,
                    @NotBlank String password) {
}
