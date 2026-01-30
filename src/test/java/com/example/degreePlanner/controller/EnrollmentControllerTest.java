package com.example.degreePlanner.controller;

import com.example.degreePlanner.dto.request.CreateEnrollmentRequest;
import com.example.degreePlanner.dto.request.UpdateEnrollmentRequest;
import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.service.CourseService;
import com.example.degreePlanner.service.EnrollmentService;
import com.example.degreePlanner.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Student student;
    private Course course1;
    private Course course2;

    @BeforeEach
    void setUp() {
        student = studentService.createStudent(new Student("AG123", "Arad", "Ganir", "arad@gmail.com"));
        course1 = courseService.createCourse(new Course("CS", "101", "Intro to CS", "Basics of CS", 3));
        course2 = courseService.createCourse(new Course("CS", "201", "Data Structures", "DS and Algorithms", 3));
    }

    // ==================== enrollStudent ====================

    @Test
    void enrollStudent_validRequest_returns201() throws Exception {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest("CS", "101", "Fall 2024");

        mockMvc.perform(post("/students/{studentId}/enrollments", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS"))
                .andExpect(jsonPath("$.courseNum").value("101"))
                .andExpect(jsonPath("$.semester").value("Fall 2024"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void enrollStudent_studentNotFound_returns404() throws Exception {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest("CS", "101", "Fall 2024");

        mockMvc.perform(post("/students/{studentId}/enrollments", 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void enrollStudent_courseNotFound_returns404() throws Exception {
        CreateEnrollmentRequest request = new CreateEnrollmentRequest("FAKE", "999", "Fall 2024");

        mockMvc.perform(post("/students/{studentId}/enrollments", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void enrollStudent_alreadyEnrolled_returns409() throws Exception {
        // First enrollment
        enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        // Try to enroll again in same semester
        CreateEnrollmentRequest request = new CreateEnrollmentRequest("CS", "101", "Fall 2024");

        mockMvc.perform(post("/students/{studentId}/enrollments", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isConflict());
    }

    // ==================== getEnrollments ====================

    @Test
    void getEnrollments_returns200() throws Exception {
        enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");
        enrollmentService.enrollStudent(student.getId(), course2.getId(), "Fall 2024");

        mockMvc.perform(get("/students/{studentId}/enrollments", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].courseCode").value("CS"))
                .andExpect(jsonPath("$[1].courseCode").value("CS"));
    }

    // ==================== updateEnrollment ====================

    @Test
    void updateEnrollment_validRequest_returns200() throws Exception {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        UpdateEnrollmentRequest request = new UpdateEnrollmentRequest(Grade.A, EnrollmentStatus.COMPLETED);

        mockMvc.perform(put("/students/{studentId}/enrollments/{enrollmentId}", student.getId(), enrollment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("A"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void updateEnrollment_notFound_returns404() throws Exception {
        UpdateEnrollmentRequest request = new UpdateEnrollmentRequest(Grade.A, EnrollmentStatus.COMPLETED);

        mockMvc.perform(put("/students/{studentId}/enrollments/{enrollmentId}", student.getId(), 9999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(request)))
                .andExpect(status().isNotFound());
    }

    // ==================== removeEnrollment ====================

    @Test
    void removeEnrollment_exists_returns204() throws Exception {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course1.getId(), "Fall 2024");

        mockMvc.perform(delete("/students/{studentId}/enrollments/{enrollmentId}", student.getId(), enrollment.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeEnrollment_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/students/{studentId}/enrollments/{enrollmentId}", student.getId(), 9999L))
                .andExpect(status().isNotFound());
    }
}
