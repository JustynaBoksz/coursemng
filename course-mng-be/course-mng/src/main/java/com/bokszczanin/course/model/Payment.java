package com.bokszczanin.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    private UUID id;
    private UUID studentId;
    private UUID courseId;
    private String courseName;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
}