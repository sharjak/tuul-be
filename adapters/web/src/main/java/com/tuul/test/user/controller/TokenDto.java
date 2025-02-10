package com.tuul.test.user.controller;

import java.time.LocalDateTime;

record TokenDto(String token, LocalDateTime expiryDate) {
}
