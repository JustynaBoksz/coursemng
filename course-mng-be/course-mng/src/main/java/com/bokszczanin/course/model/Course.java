package com.bokszczanin.course.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    private UUID id;
    private String name;
    private String description;
    private UUID createdBy;

    @Column(precision = 10, scale = 2)
    private BigDecimal price;

    private int enrolledCount;

    // Gettery, settery, konstruktor itp.
}