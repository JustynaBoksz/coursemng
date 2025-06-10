package com.bokszczanin.course.dto;

import com.bokszczanin.course.model.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID courseId,
        String courseName,
        BigDecimal coursePrice,
        PaymentStatus status
) {
}
