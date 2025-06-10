package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.CartItemResponse;
import com.bokszczanin.course.exception.NotFoundException;
import com.bokszczanin.course.model.CartItem;
import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.repository.CartRepository;
import com.bokszczanin.course.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartRepository cartRepository;
    private final CourseRepository courseRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@RequestBody CartItem item) {
        item.setId(UUID.randomUUID());
        cartRepository.save(item);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/student/{studentId}")
    public List<CartItemResponse> getCart(@PathVariable UUID studentId) {
        return cartRepository.findByStudentId(studentId).stream()
                .map(item -> {
                    Course course = courseRepository.findById(item.getCourseId())
                            .orElseThrow(() -> new NotFoundException("Course not found"));
                    return new CartItemResponse(
                            item.getId(),
                            item.getCourseId(),
                            course.getName(),
                            course.getPrice()
                    );
                })
                .collect(Collectors.toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable UUID id) {
        cartRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}