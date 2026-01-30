package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.repository.*;
import com.example.degreePlanner.service.EnrollmentService;
import com.example.degreePlanner.service.StudentMajorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProgressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RequirementRepository requirementRepository;

    @Autowired
    private StudentMajorService studentMajorService;

    @Autowired
    private EnrollmentService enrollmentService;

    private Student student;
    private Major csMajor;
    private Major mathMajor;
    private Course cs101;
    private Course cs201;
    private Requirement coreRequirement;

    @BeforeEach
    void setUp() {
        // Create student
        student = studentRepository.save(new Student("AG123", "Arad", "Ganir", "arad@gmail.com"));

        // Create majors
        csMajor = majorRepository.save(new Major("Computer Science", "CS", "BS", "CS degree", 30));
        mathMajor = majorRepository.save(new Major("Mathematics", "MATH", "BS", "Math degree", 24));

        // Create courses
        cs101 = courseRepository.save(new Course("CS", "101", "Intro to CS", "Basics", 3));
        cs201 = courseRepository.save(new Course("CS", "201", "Data Structures", "DS", 3));

        // Create requirement for CS major
        coreRequirement = new Requirement(csMajor, RequirementType.CORE, "Core Courses", null, "Required core courses");
        coreRequirement.getCourses().add(cs101);
        coreRequirement.getCourses().add(cs201);
        coreRequirement = requirementRepository.save(coreRequirement);
    }

    // ==================== getOverallProgress ====================

    @Test
    void getOverallProgress_returns200() throws Exception {
        // Declare major for student
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);

        mockMvc.perform(get("/students/{id}/progress", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].major.code").value("CS"))
                .andExpect(jsonPath("$[0].major.designation").value("BS"))
                .andExpect(jsonPath("$[0].totalCreditsRequired").value(30))
                .andExpect(jsonPath("$[0].creditsCompleted").value(0))
                .andExpect(jsonPath("$[0].percentComplete").value(0.0));
    }

    @Test
    void getOverallProgress_withCompletedCourses_returns200() throws Exception {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);

        mockMvc.perform(get("/students/{id}/progress", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].creditsCompleted").value(3))
                .andExpect(jsonPath("$[0].creditsRemaining").value(27))
                .andExpect(jsonPath("$[0].percentComplete").value(10.0));
    }

    @Test
    void getOverallProgress_multipleMajors_returns200() throws Exception {
        // Create requirement for math major
        Requirement mathReq = new Requirement(mathMajor, RequirementType.CORE, "Math Core", null, "Math courses");
        requirementRepository.save(mathReq);

        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        studentMajorService.declareMajor(student.getId(), "MATH", "BS", false);

        mockMvc.perform(get("/students/{id}/progress", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getOverallProgress_noMajors_returnsEmptyList() throws Exception {
        // Student has no declared majors

        mockMvc.perform(get("/students/{id}/progress", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOverallProgress_studentNotFound_returns404() throws Exception {
        mockMvc.perform(get("/students/{id}/progress", 9999L))
                .andExpect(status().isNotFound());
    }

    // ==================== getMajorProgress ====================

    @Test
    void getMajorProgress_returns200() throws Exception {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);

        mockMvc.perform(get("/students/{id}/progress/{code}/{designation}",
                        student.getId(), "CS", "BS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.major.code").value("CS"))
                .andExpect(jsonPath("$.major.designation").value("BS"))
                .andExpect(jsonPath("$.major.name").value("Computer Science"))
                .andExpect(jsonPath("$.totalCreditsRequired").value(30))
                .andExpect(jsonPath("$.creditsCompleted").value(0))
                .andExpect(jsonPath("$.creditsRemaining").value(30))
                .andExpect(jsonPath("$.percentComplete").value(0.0))
                .andExpect(jsonPath("$.requirements.length()").value(1));
    }

    @Test
    void getMajorProgress_withProgress_returns200() throws Exception {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);
        completeEnrollment(student, cs201);

        mockMvc.perform(get("/students/{id}/progress/{code}/{designation}",
                        student.getId(), "CS", "BS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.creditsCompleted").value(6))
                .andExpect(jsonPath("$.creditsRemaining").value(24))
                .andExpect(jsonPath("$.percentComplete").value(20.0))
                .andExpect(jsonPath("$.requirements[0].status").value("COMPLETE"))
                .andExpect(jsonPath("$.requirements[0].completedCourses.length()").value(2))
                .andExpect(jsonPath("$.requirements[0].remainingCourses.length()").value(0));
    }

    @Test
    void getMajorProgress_requirementInProgress_returns200() throws Exception {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);  // Only one of two required courses

        mockMvc.perform(get("/students/{id}/progress/{code}/{designation}",
                        student.getId(), "CS", "BS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requirements[0].status").value("IN_PROGRESS"))
                .andExpect(jsonPath("$.requirements[0].completedCourses.length()").value(1))
                .andExpect(jsonPath("$.requirements[0].remainingCourses.length()").value(1));
    }

    @Test
    void getMajorProgress_majorNotDeclared_returns404() throws Exception {
        // Student exists but hasn't declared CS major

        mockMvc.perform(get("/students/{id}/progress/{code}/{designation}",
                        student.getId(), "CS", "BS"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMajorProgress_majorDoesNotExist_returns404() throws Exception {
        mockMvc.perform(get("/students/{id}/progress/{code}/{designation}",
                        student.getId(), "FAKE", "BS"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getMajorProgress_studentNotFound_returns404() throws Exception {
        mockMvc.perform(get("/students/{id}/progress/{code}/{designation}",
                        9999L, "CS", "BS"))
                .andExpect(status().isNotFound());
    }

    // ==================== Helper Methods ====================

    private void completeEnrollment(Student student, Course course) {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId(), "Fall 2024");
        enrollmentService.updateEnrollment(enrollment.getId(), Grade.A, EnrollmentStatus.COMPLETED);
    }
}
