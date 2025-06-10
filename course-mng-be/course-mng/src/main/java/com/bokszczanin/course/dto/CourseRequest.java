package com.bokszczanin.course.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CourseRequest(
        @NotBlank String name,
        @NotBlank String description,
        @NotNull BigDecimal price
) {
}
