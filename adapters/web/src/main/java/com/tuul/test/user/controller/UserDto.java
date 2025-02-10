package com.tuul.test.user.controller;

import java.util.UUID;

record UserDto(UUID id, String name, String email) {
}
