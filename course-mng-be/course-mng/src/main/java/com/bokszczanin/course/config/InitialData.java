package com.bokszczanin.course.config;

import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.model.Role;
import com.bokszczanin.course.model.UserData;
import com.bokszczanin.course.repository.CourseRepository;
import com.bokszczanin.course.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
public class InitialData {
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner loadCourses(CourseRepository courseRepository) {
        return args -> {
            if (courseRepository.count() == 0) {
                courseRepository.save(Course.builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .name("Kurs Java: podstawy")
                        .price(BigDecimal.valueOf(7000))
                        .description("Stacjonarny, 1x w tygodniu, 20 tygodni")
                        .build());

                courseRepository.save(Course.builder()
                        .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .name("Kurs Java: zaawansowany")
                        .description("Zdalny, 3x w tygodniu, 6 tygodni")
                        .build());

                courseRepository.save(Course.builder()
                        .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                        .name("Kurs Python: podstawy")
                        .description("Stacjonarny, 2x w tygodniu, 12 tygodni")
                        .build());

                courseRepository.save(Course.builder()
                        .id(UUID.fromString("44444444-4444-4444-4444-444444444444"))
                        .name("Kurs Python: AI/ML")
                        .price(BigDecimal.valueOf(12000))
                        .description("Zdalny, 4x w tygodniu, 8 tygodni")
                        .build());
            }
        };
    }

    @Bean
    CommandLineRunner loadUsers(UserRepository userRepository) {
        return args -> {
            if (userRepository.count() == 0) {
                userRepository.save(UserData.builder()
                        .id(UUID.fromString("11111111-1111-1111-1111-111111111111"))
                        .username("student")
                        .email("student@example.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.STUDENT)
                        .build());

                userRepository.save(UserData.builder()
                        .id(UUID.fromString("22222222-2222-2222-2222-222222222222"))
                        .username("lecturer")
                        .email("lecturer@example.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.LECTURER)
                        .build());

                userRepository.save(UserData.builder()
                        .id(UUID.fromString("33333333-3333-3333-3333-333333333333"))
                        .username("admin")
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("123456"))
                        .role(Role.ADMIN)
                        .build());
            }
        };
    }
}