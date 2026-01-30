package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.repository.*;
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
public class ProgressServiceTest {

    @Autowired
    private ProgressService progressService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentMajorService studentMajorService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RequirementRepository requirementRepository;

    private Student student;
    private Major csMajor;
    private Major mathMajor;
    private Course cs101;
    private Course cs201;
    private Course cs301;
    private Course math101;
    private Course math201;
    private Requirement coreRequirement;
    private Requirement electiveRequirement;

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
        cs301 = courseRepository.save(new Course("CS", "301", "Algorithms", "Algo", 3));
        math101 = courseRepository.save(new Course("MATH", "101", "Calculus I", "Calc", 4));
        math201 = courseRepository.save(new Course("MATH", "201", "Calculus II", "Calc 2", 4));

        // Create requirements for CS major
        // Core requirement: must complete all courses (no minCredits)
        coreRequirement = new Requirement(csMajor, RequirementType.CORE, "Core Courses", null, "Required core courses");
        coreRequirement.getCourses().add(cs101);
        coreRequirement.getCourses().add(cs201);
        coreRequirement = requirementRepository.save(coreRequirement);

        // Elective requirement: need 6 credits from list (minCredits set)
        electiveRequirement = new Requirement(csMajor, RequirementType.ELECTIVE, "Electives", 6, "Choose electives");
        electiveRequirement.getCourses().add(cs301);
        electiveRequirement.getCourses().add(math101);
        electiveRequirement.getCourses().add(math201);
        electiveRequirement = requirementRepository.save(electiveRequirement);
    }

    // ==================== getMajorProgress ====================

    @Test
    void getMajorProgress_calculatesCreditsCorrectly() {
        // Declare major and complete some courses
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);  // 3 credits
        completeEnrollment(student, cs201);  // 3 credits

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        assertEquals(30, progress.totalCreditsRequired());
        assertEquals(6, progress.creditsCompleted());  // cs101 (3) + cs201 (3)
        assertEquals(24, progress.creditsRemaining());
        assertEquals(20.0, progress.percentComplete(), 0.01);  // 6/30 = 20%
    }

    @Test
    void getMajorProgress_noCoursesCompleted_returnsZeroProgress() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        assertEquals(30, progress.totalCreditsRequired());
        assertEquals(0, progress.creditsCompleted());
        assertEquals(30, progress.creditsRemaining());
        assertEquals(0.0, progress.percentComplete(), 0.01);
    }

    @Test
    void getMajorProgress_allCoursesCompleted_returns100Percent() {
        // Create a simpler major with just 6 credits required
        Major simpleMajor = majorRepository.save(new Major("Simple", "SIMP", "BS", "Simple major", 6));
        Requirement simpleReq = new Requirement(simpleMajor, RequirementType.CORE, "All", null, "All courses");
        simpleReq.getCourses().add(cs101);  // 3 credits
        simpleReq.getCourses().add(cs201);  // 3 credits
        requirementRepository.save(simpleReq);

        studentMajorService.declareMajor(student.getId(), "SIMP", "BS", true);
        completeEnrollment(student, cs101);
        completeEnrollment(student, cs201);

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "SIMP", "BS");

        assertEquals(6, progress.totalCreditsRequired());
        assertEquals(6, progress.creditsCompleted());
        assertEquals(0, progress.creditsRemaining());
        assertEquals(100.0, progress.percentComplete(), 0.01);
    }

    @Test
    void getMajorProgress_specificCourses_tracksEachCourse() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        // Check core requirement progress
        ProgressService.RequirementProgress coreProgress = progress.requirements().stream()
                .filter(r -> r.requirement().getName().equals("Core Courses"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, coreProgress.completedCourses().size());
        assertEquals(1, coreProgress.remainingCourses().size());
        assertTrue(coreProgress.completedCourses().stream()
                .anyMatch(c -> c.getId().equals(cs101.getId())));
        assertTrue(coreProgress.remainingCourses().stream()
                .anyMatch(c -> c.getId().equals(cs201.getId())));
    }

    @Test
    void getMajorProgress_creditsFromList_tracksCreditsTowardRequirement() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, math101);  // 4 credits toward elective

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        // Check elective requirement progress (needs 6 credits)
        ProgressService.RequirementProgress electiveProgress = progress.requirements().stream()
                .filter(r -> r.requirement().getName().equals("Electives"))
                .findFirst()
                .orElseThrow();

        assertEquals(4, electiveProgress.creditsCompleted());
        assertEquals(2, electiveProgress.creditsRemaining());  // 6 - 4 = 2
        assertEquals(RequirementStatus.IN_PROGRESS, electiveProgress.status());
    }

    @Test
    void getMajorProgress_coursesFromList_tracksCountTowardRequirement() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        // Check core requirement progress (no minCredits = need all courses)
        ProgressService.RequirementProgress coreProgress = progress.requirements().stream()
                .filter(r -> r.requirement().getName().equals("Core Courses"))
                .findFirst()
                .orElseThrow();

        assertEquals(1, coreProgress.completedCourses().size());
        assertEquals(1, coreProgress.remainingCourses().size());
    }

    @Test
    void getMajorProgress_requirementComplete_statusComplete() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        // Complete all core courses
        completeEnrollment(student, cs101);
        completeEnrollment(student, cs201);

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        ProgressService.RequirementProgress coreProgress = progress.requirements().stream()
                .filter(r -> r.requirement().getName().equals("Core Courses"))
                .findFirst()
                .orElseThrow();

        assertEquals(RequirementStatus.COMPLETE, coreProgress.status());
        assertTrue(coreProgress.remainingCourses().isEmpty());
    }

    @Test
    void getMajorProgress_requirementPartial_statusInProgress() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        // Complete only one core course
        completeEnrollment(student, cs101);

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        ProgressService.RequirementProgress coreProgress = progress.requirements().stream()
                .filter(r -> r.requirement().getName().equals("Core Courses"))
                .findFirst()
                .orElseThrow();

        assertEquals(RequirementStatus.IN_PROGRESS, coreProgress.status());
    }

    @Test
    void getMajorProgress_requirementNotStarted_statusNotStarted() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        // Don't complete any courses

        ProgressService.MajorProgress progress = progressService.getMajorProgress(
                student.getId(), "CS", "BS");

        ProgressService.RequirementProgress coreProgress = progress.requirements().stream()
                .filter(r -> r.requirement().getName().equals("Core Courses"))
                .findFirst()
                .orElseThrow();

        assertEquals(RequirementStatus.NOT_STARTED, coreProgress.status());
        assertEquals(0, coreProgress.creditsCompleted());
    }

    // ==================== getOverallProgress ====================

    @Test
    void getOverallProgress_multipleMajors_returnsAll() {
        // Create requirement for math major
        Requirement mathReq = new Requirement(mathMajor, RequirementType.CORE, "Math Core", null, "Math courses");
        mathReq.getCourses().add(math101);
        mathReq.getCourses().add(math201);
        requirementRepository.save(mathReq);

        // Declare both majors
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        studentMajorService.declareMajor(student.getId(), "MATH", "BS", false);

        List<ProgressService.MajorProgress> progressList = progressService.getOverallProgress(student.getId());

        assertEquals(2, progressList.size());
        assertTrue(progressList.stream()
                .anyMatch(p -> p.major().getCode().equals("CS")));
        assertTrue(progressList.stream()
                .anyMatch(p -> p.major().getCode().equals("MATH")));
    }

    @Test
    void getOverallProgress_noMajors_returnsEmptyList() {
        // Student has no declared majors

        List<ProgressService.MajorProgress> progressList = progressService.getOverallProgress(student.getId());

        assertTrue(progressList.isEmpty());
    }

    // ==================== getRequirementProgress ====================

    @Test
    void getRequirementProgress_returnsCorrectProgress() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        completeEnrollment(student, cs101);

        ProgressService.RequirementProgress progress = progressService.getRequirementProgress(
                student.getId(), coreRequirement.getId());

        assertEquals(coreRequirement.getId(), progress.requirement().getId());
        assertEquals(RequirementStatus.IN_PROGRESS, progress.status());
        assertEquals(1, progress.completedCourses().size());
        assertEquals(1, progress.remainingCourses().size());
    }

    // ==================== calculateCreditsCompleted ====================

    @Test
    void calculateCreditsCompleted_sumsOnlyMajorCourses() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);

        // Complete courses in the major
        completeEnrollment(student, cs101);  // 3 credits - in CS major
        completeEnrollment(student, cs201);  // 3 credits - in CS major
        completeEnrollment(student, math101); // 4 credits - in CS major (elective)

        int credits = progressService.calculateCreditsCompleted(student.getId(), csMajor.getId());

        assertEquals(10, credits);  // 3 + 3 + 4 = 10
    }

    @Test
    void calculateCreditsCompleted_excludesCoursesNotInMajor() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);

        // Create and complete a course not in any CS requirement
        Course unrelatedCourse = courseRepository.save(
                new Course("ART", "101", "Intro to Art", "Art basics", 3));
        completeEnrollment(student, unrelatedCourse);
        completeEnrollment(student, cs101);

        int credits = progressService.calculateCreditsCompleted(student.getId(), csMajor.getId());

        assertEquals(3, credits);  // Only cs101, not ART 101
    }

    // ==================== getCompletedRequirements ====================

    @Test
    void getCompletedRequirements_returnsOnlyCompleted() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        // Complete all core courses
        completeEnrollment(student, cs101);
        completeEnrollment(student, cs201);

        List<Requirement> completed = progressService.getCompletedRequirements(
                student.getId(), csMajor.getId());

        assertEquals(1, completed.size());
        assertEquals("Core Courses", completed.get(0).getName());
    }

    // ==================== getRemainingRequirements ====================

    @Test
    void getRemainingRequirements_returnsUnfulfilled() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        // Complete all core courses but not electives
        completeEnrollment(student, cs101);
        completeEnrollment(student, cs201);

        List<Requirement> remaining = progressService.getRemainingRequirements(
                student.getId(), csMajor.getId());

        assertEquals(1, remaining.size());
        assertEquals("Electives", remaining.get(0).getName());
    }

    @Test
    void getRemainingRequirements_includesPartiallyComplete() {
        studentMajorService.declareMajor(student.getId(), "CS", "BS", true);
        // Complete one core course (partial)
        completeEnrollment(student, cs101);

        List<Requirement> remaining = progressService.getRemainingRequirements(
                student.getId(), csMajor.getId());

        assertEquals(2, remaining.size());  // Both core (partial) and elective (not started)
    }

    // ==================== Helper Methods ====================

    private void completeEnrollment(Student student, Course course) {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId(), "Fall 2024");
        enrollmentService.updateEnrollment(enrollment.getId(), Grade.A, EnrollmentStatus.COMPLETED);
    }
}
