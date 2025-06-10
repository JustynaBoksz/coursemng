package com.bokszczanin.course.dto;

import com.bokszczanin.course.model.Role;

public record RegisterRequest(String username, String email, String password, Role role) {
}