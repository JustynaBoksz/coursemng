package com.bokszczanin.course.controller;

import com.bokszczanin.course.dto.CourseRequest;
import com.bokszczanin.course.model.Course;
import com.bokszczanin.course.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseControllerTest {

    @InjectMocks
    private CourseController courseController;

    @Mock
    private CourseRepository courseRepository;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnAllCourses() {
        List<Course> mockCourses = List.of(new Course(), new Course());
        when(courseRepository.findAll()).thenReturn(mockCourses);

        List<Course> result = courseController.findAll();

        assertEquals(2, result.size());
    }

    @Test
    void shouldSearchByName() {
        String name = "java";
        List<Course> mockCourses = List.of(new Course());
        when(courseRepository.findByNameContainingIgnoreCase(name)).thenReturn(mockCourses);

        List<Course> result = courseController.search(name);

        assertEquals(1, result.size());
    }

    @Test
    void shouldFilterCourses() {
        List<Course> filtered = List.of(new Course());
        when(courseRepository.filterCourses(null, null, null)).thenReturn(filtered);

        List<Course> result = courseController.filterCourses(null, null, null);

        assertEquals(1, result.size());
    }

    @Test
    void shouldCreateCourse() {
        CourseRequest request = new CourseRequest("Spring Boot", "backend", new BigDecimal("299.00"));

        ResponseEntity<?> response = courseController.createCourse(request);

        assertEquals(200, response.getStatusCodeValue());
        verify(courseRepository).save(any(Course.class));
        Course saved = (Course) response.getBody();
        assertEquals("Spring Boot", saved.getName());
        assertEquals("backend", saved.getDescription());
        assertEquals(new BigDecimal("299.00"), saved.getPrice());
    }

    @Test
    void shouldUpdateExistingCourse() {
        UUID id = UUID.randomUUID();
        Course existing = new Course();
        existing.setId(id);
        existing.setName("Old");
        existing.setDescription("Old Desc");
        existing.setPrice(new BigDecimal("100"));

        Course updated = new Course();
        updated.setName("New");
        updated.setDescription("New Desc");
        updated.setPrice(new BigDecimal("150"));

        when(courseRepository.findById(id)).thenReturn(Optional.of(existing));

        ResponseEntity<?> response = courseController.update(id, updated);

        assertEquals(200, response.getStatusCodeValue());
        verify(courseRepository).save(existing);
        assertEquals("New", existing.getName());
    }

    @Test
    void shouldReturnNotFoundOnUpdateIfCourseMissing() {
        UUID id = UUID.randomUUID();
        when(courseRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = courseController.update(id, new Course());

        assertEquals(404, response.getStatusCodeValue());
    }

    @Test
    void shouldDeleteCourse() {
        UUID id = UUID.randomUUID();

        ResponseEntity<?> response = courseController.delete(id);

        assertEquals(204, response.getStatusCodeValue());
        verify(courseRepository).deleteById(id);
    }

    @Test
    void shouldReturnPopularCoursesSortedByEnrolledCountDesc() {
        Course c1 = new Course();
        c1.setEnrolledCount(5);
        Course c2 = new Course();
        c2.setEnrolledCount(10);

        when(courseRepository.findAll()).thenReturn(List.of(c1, c2));

        List<Course> result = courseController.popular();

        assertEquals(2, result.size());
        assertEquals(10, result.get(0).getEnrolledCount());
    }
}
