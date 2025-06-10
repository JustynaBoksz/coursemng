package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.PaymentResponse;
import com.bokszczanin.course.exception.NotFoundException;
import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.model.Payment;
import com.bokszczanin.course.model.PaymentStatus;
import com.bokszczanin.course.repository.CartRepository;
import com.bokszczanin.course.repository.CourseRepository;
import com.bokszczanin.course.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentControllerTest {

    @InjectMocks
    private PaymentController paymentController;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CartRepository cartRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldStartPaymentAndIncreaseEnrollment() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Course course = Course.builder()
                .id(courseId)
                .name("Java Advanced")
                .price(new BigDecimal("200.00"))
                .enrolledCount(3)
                .build();

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        Map<String, String> body = Map.of(
                "studentId", studentId.toString(),
                "courseId", courseId.toString()
        );

        ResponseEntity<?> response = paymentController.startPayment(body);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Payment successful", ((Map<?, ?>) response.getBody()).get("message"));

        verify(paymentRepository).save(any(Payment.class));
        verify(courseRepository).save(course);
        assertEquals(4, course.getEnrolledCount());
    }

    @Test
    void shouldReturnStudentPaymentsWithCourseInfo() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .studentId(studentId)
                .courseId(courseId)
                .status(PaymentStatus.COMPLETED)
                .timestamp(LocalDateTime.now())
                .build();

        Course course = Course.builder()
                .id(courseId)
                .name("Spring Boot")
                .price(new BigDecimal("149.99"))
                .build();

        when(paymentRepository.findByStudentId(studentId)).thenReturn(List.of(payment));
        when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

        List<PaymentResponse> result = paymentController.getStudentPayments(studentId);

        assertEquals(1, result.size());
        PaymentResponse res = result.get(0);
        assertEquals(course.getName(), res.courseName());
        assertEquals(course.getPrice(), res.coursePrice());
    }

    @Test
    void shouldThrowIfCourseMissingInPaymentMapping() {
        UUID studentId = UUID.randomUUID();
        UUID courseId = UUID.randomUUID();

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .studentId(studentId)
                .courseId(courseId)
                .build();

        when(paymentRepository.findByStudentId(studentId)).thenReturn(List.of(payment));
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> paymentController.getStudentPayments(studentId));
    }

    @Test
    void shouldGroupPaymentsByStatus() {
        Payment p1 = Payment.builder().id(UUID.randomUUID()).status(PaymentStatus.COMPLETED).build();
        Payment p2 = Payment.builder().id(UUID.randomUUID()).status(PaymentStatus.FAILED).build();
        Payment p3 = Payment.builder().id(UUID.randomUUID()).status(PaymentStatus.COMPLETED).build();

        when(paymentRepository.findAll()).thenReturn(List.of(p1, p2, p3));

        Map<PaymentStatus, List<Payment>> result = paymentController.getPaymentsGroupedByStatus();

        assertEquals(2, result.get(PaymentStatus.COMPLETED).size());
        assertEquals(1, result.get(PaymentStatus.FAILED).size());
    }
}
