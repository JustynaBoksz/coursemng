package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.PaymentResponse;
import com.bokszczanin.course.exception.NotFoundException;
import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.model.Payment;
import com.bokszczanin.course.model.PaymentStatus;
import com.bokszczanin.course.repository.CartRepository;
import com.bokszczanin.course.repository.CourseRepository;
import com.bokszczanin.course.repository.PaymentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@Log
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final CourseRepository courseRepository;
    private final CartRepository cartRepository;

    @PostMapping("/start")
    public ResponseEntity<?> startPayment(@RequestBody Map<String, String> body) {
        UUID studentId = UUID.fromString(body.get("studentId"));
        UUID courseId = UUID.fromString(body.get("courseId"));

        Payment payment = Payment.builder()
                .id(UUID.randomUUID())
                .studentId(studentId)
                .courseId(courseId)
                .timestamp(LocalDateTime.now())
                .status(PaymentStatus.COMPLETED)
                .build();

        paymentRepository.save(payment);

        courseRepository.findById(courseId).ifPresent(course -> {
            course.setEnrolledCount(course.getEnrolledCount() + 1);
            courseRepository.save(course);
        });

        return ResponseEntity.ok(Map.of("message", "Payment successful"));
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@RequestBody Map<String, String> body) {
        Stripe.apiKey = "sk_test_51RXLnZFh71ZWNWsJ2EZi2tjC7rEdNc53s0Cj0MMGetcQ7V0BdEzndh7spQFPxqmWwAWvFdA2fRglSkt1c0c1BbQD00Mk4Yb3Pl";

        UUID courseId = UUID.fromString(body.get("courseId"));
        UUID studentId = UUID.fromString(body.get("studentId"));
        String courseName = body.get("courseName");

        Course course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Nie ma kursu"));

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:4000/dashboard?success=true")
                .setCancelUrl("http://localhost:4000/dashboard?cancel=true")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("pln")
                                                .setUnitAmount(!Objects.isNull(course.getPrice()) ? course.getPrice().multiply(BigDecimal.valueOf(100)).longValue() : BigDecimal.ZERO.longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(course.getName())
                                                                .build()
                                                ).build()
                                )
                                .setQuantity(1L)
                                .build()
                )
                .putMetadata("studentId", studentId.toString())
                .putMetadata("courseId", courseId.toString())
                .putMetadata("courseName", courseName)
                .build();

        try {
            Session session = Session.create(params);
            return ResponseEntity.ok(Map.of("url", session.getUrl()));
        } catch (StripeException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // stripe CLI: https://docs.stripe.com/stripe-cli?install-method=windows
    // zip download: https://github.com/stripe/stripe-cli/releases/tag/v1.27.0
    // stripe login
    // stripe listen --forward-to localhost:8060/payment/webhook --events checkout.session.completed
    // test card: 4242 4242 4242 4242 / 12 / 34
    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        String endpointSecret = "whsec_2dfa0f15e8c56de2f6276030279fd32ddd3a1f2ff96018d534f87dbef7c61089";
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            log.info("Stripe event processing, type: " + event.getType().toString());
            if ("checkout.session.completed".equals(event.getType())) {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(payload);
                String sessionId = root.path("data").path("object").path("id").asText();

                Session session = Session.retrieve(sessionId);
                String studentIdStr = session.getMetadata().get("studentId");
                String courseIdStr = session.getMetadata().get("courseId");
                String courseNameStr = session.getMetadata().get("courseName");

                if (studentIdStr != null && courseIdStr != null) {
                    UUID studentId = UUID.fromString(studentIdStr);
                    UUID courseId = UUID.fromString(courseIdStr);

                    paymentRepository.save(Payment.builder()
                            .id(UUID.randomUUID())
                            .studentId(studentId)
                            .courseId(courseId)
                            .courseName(courseNameStr)
                            .status(PaymentStatus.COMPLETED)
                            .timestamp(LocalDateTime.now())
                            .build());

                    courseRepository.findById(courseId).ifPresent(course -> {
                        course.setEnrolledCount(course.getEnrolledCount() + 1);
                        courseRepository.save(course);
                    });

                    cartRepository.findByStudentId(studentId).stream()
                            .filter(i -> i.getCourseId().equals(courseId))
                            .forEach(i -> cartRepository.deleteById(i.getId()));
                }
            }

            return ResponseEntity.ok("");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(400).body("Webhook error: " + e.getMessage());
        }
    }

    @GetMapping("/student/{id}")
    public List<PaymentResponse> getStudentPayments(@PathVariable UUID id) {
        return paymentRepository.findByStudentId(id).stream()
                .map(p -> {
                    Course course = courseRepository.findById(p.getCourseId())
                            .orElseThrow(() -> new NotFoundException("Course not found"));
                    return new PaymentResponse(
                            p.getId(),
                            p.getCourseId(),
                            course.getName(),
                            course.getPrice(),
                            p.getStatus()
                    );
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/grouped")
    public Map<PaymentStatus, List<Payment>> getPaymentsGroupedByStatus() {
        return paymentRepository.findAll().stream()
                .collect(Collectors.groupingBy(Payment::getStatus));
    }
}