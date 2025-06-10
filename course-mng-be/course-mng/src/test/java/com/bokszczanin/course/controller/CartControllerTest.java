package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.CartItemResponse;
import com.bokszczanin.course.exception.NotFoundException;
import com.bokszczanin.course.model.CartItem;
import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.repository.CartRepository;
import com.bokszczanin.course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartControllerTest {

    @InjectMocks
    private CartController cartController;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldAddItemToCart() {
        CartItem item = new CartItem();
        item.setCourseId(UUID.randomUUID());
        item.setStudentId(UUID.randomUUID());

        ResponseEntity<?> response = cartController.addToCart(item);

        assertEquals(200, response.getStatusCodeValue());
        verify(cartRepository).save(any(CartItem.class));
        assertNotNull(item.getId());
    }

    @Test
    void shouldGetCartItems() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();
        UUID cartId = UUID.randomUUID();

        CartItem item = new CartItem();
        item.setId(cartId);
        item.setCourseId(courseId);
        item.setStudentId(studentId);

        Course course = Course.builder()
                .id(courseId)
                .name("Java 101")
                .price(new BigDecimal("199.99"))
                .build();

        when(cartRepository.findByStudentId(studentId)).thenReturn(List.of(item));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        List<CartItemResponse> result = cartController.getCart(studentId);

        assertEquals(1, result.size());
        CartItemResponse response = result.get(0);
        assertEquals(cartId, response.id());
        assertEquals(courseId, response.courseId());
        assertEquals("Java 101", response.courseName());
        assertEquals(new BigDecimal("199.99"), response.coursePrice());
    }

    @Test
    void shouldThrowWhenCourseNotFound() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setCourseId(courseId);
        item.setStudentId(studentId);

        when(cartRepository.findByStudentId(studentId)).thenReturn(List.of(item));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> cartController.getCart(studentId));
    }

    @Test
    void shouldRemoveItemFromCart() {
        UUID itemId = UUID.randomUUID();

        ResponseEntity<?> response = cartController.removeFromCart(itemId);

        assertEquals(204, response.getStatusCodeValue());
        verify(cartRepository).deleteById(itemId);
    }
}
