package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.EnrollmentRepository;
import com.example.degreePlanner.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Student student;
    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        student = studentRepository.save(new Student("AG123", "Arad", "Ganir", "arad@gmail.com"));
        course1 = courseRepository.save(new Course("CS", "101", "Intro to CS", "Basics of CS", 3));
        course2 = courseRepository.save(new Course("CS", "201", "Data Structures", "DS and Algorithms", 3));
    }

    // ==================== enrollStudent ====================

    @Test
    void enrollStudent_validData_enrollsStudent() {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        assertNotNull(enrollment.getId());
        assertEquals(student.getId(), enrollment.getStudent().getId());
        assertEquals(course1.getId(), enrollment.getCourse().getId());
        assertEquals("Fall 2024", enrollment.getSemester());
        assertEquals(EnrollmentStatus.IN_PROGRESS, enrollment.getEnrollmentStatus());
        assertNull(enrollment.getGrade());
    }

    @Test
    void enrollStudent_studentNotFound_throwsException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.enrollStudent(9999L, course1.getId(), "Fall 2024");
        });
    }

    @Test
    void enrollStudent_courseNotFound_throwsException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.enrollStudent(student.getId(), 9999L, "Fall 2024");
        });
    }

    @Test
    void enrollStudent_alreadyEnrolledSameSemester_throwsException() {
        enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        assertThrows(DuplicateResourceException.class, () -> {
            enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        });
    }

    @Test
    void enrollStudent_retakeDifferentSemester_enrollsStudent() {
        Enrollment first = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        Enrollment second = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Spring 2025");

        assertNotNull(first.getId());
        assertNotNull(second.getId());
        assertNotEquals(first.getId(), second.getId());
        assertEquals("Fall 2024", first.getSemester());
        assertEquals("Spring 2025", second.getSemester());
    }

    // ==================== getEnrollments ====================

    @Test
    void getEnrollments_returnsAllEnrollments() {
        enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        enrollmentService.enrollStudent(student.getId(), course2.getId(), "Fall 2024");

        List<Enrollment> enrollments = enrollmentService.getEnrollments(student.getId());

        assertEquals(2, enrollments.size());
    }

    @Test
    void getEnrollments_none_returnsEmptyList() {
        List<Enrollment> enrollments = enrollmentService.getEnrollments(student.getId());

        assertTrue(enrollments.isEmpty());
    }

    @Test
    void getEnrollmentsByStatus_filtersCorrectly() {
        Enrollment e1 = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        Enrollment e2 = enrollmentService.enrollStudent(student.getId(), course2.getId(), "Fall 2024");

        // Update one to COMPLETED
        enrollmentService.updateEnrollment(e1.getId(), Grade.A, EnrollmentStatus.COMPLETED);

        List<Enrollment> completed = enrollmentService.getEnrollmentsByStatus(student.getId(), EnrollmentStatus.COMPLETED);
        List<Enrollment> inProgress = enrollmentService.getEnrollmentsByStatus(student.getId(), EnrollmentStatus.IN_PROGRESS);

        assertEquals(1, completed.size());
        assertEquals(1, inProgress.size());
        assertEquals(course1.getId(), completed.get(0).getCourse().getId());
        assertEquals(course2.getId(), inProgress.get(0).getCourse().getId());
    }

    // ==================== updateEnrollment ====================

    @Test
    void updateEnrollment_validData_updatesEnrollment() {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        Enrollment updated = enrollmentService.updateEnrollment(enrollment.getId(), Grade.B_PLUS, EnrollmentStatus.COMPLETED);

        assertEquals(Grade.B_PLUS, updated.getGrade());
        assertEquals(EnrollmentStatus.COMPLETED, updated.getEnrollmentStatus());
    }

    @Test
    void updateEnrollment_notFound_throwsException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.updateEnrollment(9999L, Grade.A, EnrollmentStatus.COMPLETED);
        });
    }

    @Test
    void updateEnrollment_setGradeAndStatus_updatesCorrectly() {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        // First update - set to completed with grade
        Enrollment updated1 = enrollmentService.updateEnrollment(enrollment.getId(), Grade.A_MINUS, EnrollmentStatus.COMPLETED);
        assertEquals(Grade.A_MINUS, updated1.getGrade());
        assertEquals(EnrollmentStatus.COMPLETED, updated1.getEnrollmentStatus());

        // Second update - change grade
        Enrollment updated2 = enrollmentService.updateEnrollment(enrollment.getId(), Grade.A_MINUS, EnrollmentStatus.COMPLETED);
        assertEquals(Grade.A_MINUS, updated2.getGrade());
    }

    // ==================== removeEnrollment ====================

    @Test
    void removeEnrollment_exists_removesEnrollment() {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        Long enrollmentId = enrollment.getId();

        enrollmentService.removeEnrollment(enrollmentId);

        assertFalse(enrollmentRepository.existsById(enrollmentId));
    }

    @Test
    void removeEnrollment_notFound_throwsException() {
        assertThrows(ResourceNotFoundException.class, () -> {
            enrollmentService.removeEnrollment(9999L);
        });
    }

    // ==================== getCompletedCourses ====================

    @Test
    void getCompletedCourses_returnsOnlyCompleted() {
        Enrollment e1 = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        Enrollment e2 = enrollmentService.enrollStudent(student.getId(), course2.getId(), "Fall 2024");

        // Complete only the first one
        enrollmentService.updateEnrollment(e1.getId(), Grade.A, EnrollmentStatus.COMPLETED);

        List<Course> completed = enrollmentService.getCompletedCourses(student.getId());

        assertEquals(1, completed.size());
        assertEquals(course1.getId(), completed.get(0).getId());
    }
}