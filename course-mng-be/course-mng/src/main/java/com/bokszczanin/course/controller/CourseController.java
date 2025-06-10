package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.CourseRequest;
import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {
    private final CourseRepository courseRepository;

    @GetMapping
    public List<Course> findAll() {
        return courseRepository.findAll();
    }

    @GetMapping("/search")
    public List<Course> search(@RequestParam String name) {
        return courseRepository.findByNameContainingIgnoreCase(name);
    }

    @GetMapping("/filter")
    public List<Course> filterCourses(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        return courseRepository.filterCourses(name, minPrice, maxPrice);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public ResponseEntity<?> createCourse(@RequestBody @Valid CourseRequest request) {
        Course course = new Course();
        course.setId(UUID.randomUUID());
        course.setName(request.name());
        course.setDescription(request.description());
        course.setPrice(request.price());
        course.setEnrolledCount(0);
        courseRepository.save(course);
        return ResponseEntity.ok(course);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public ResponseEntity<?> update(@PathVariable UUID id, @RequestBody Course updated) {
        return courseRepository.findById(id).map(existing -> {
            existing.setName(updated.getName());
            existing.setDescription(updated.getDescription());
            existing.setPrice(updated.getPrice());
            courseRepository.save(existing);
            return ResponseEntity.ok().build();
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public ResponseEntity<?> delete(@PathVariable UUID id) {
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/popular")
    @PreAuthorize("hasAnyRole('ADMIN','LECTURER')")
    public List<Course> popular() {
        return courseRepository.findAll().stream()
                .sorted((a, b) -> b.getEnrolledCount() - a.getEnrolledCount())
                .toList();
    }
}