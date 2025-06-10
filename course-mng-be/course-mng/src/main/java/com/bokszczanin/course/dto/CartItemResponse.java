package com.bokszczanin.course.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID id,
        UUID courseId,
        String courseName,
        BigDecimal coursePrice
) {}
