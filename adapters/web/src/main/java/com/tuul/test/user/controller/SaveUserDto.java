package com.tuul.test.user.controller;

import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.Email;

record SaveUserDto(@NotBlank String name,
                   @Email @NotBlank String email,
                   @NotBlank String password) {
}
