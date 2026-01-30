package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EligibilityServiceTest {

    @Autowired
    private EligibilityService eligibilityService;

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private PrerequisiteRepository prerequisiteRepository;

    @Autowired
    private PrerequisiteItemRepository prerequisiteItemRepository;

    private Student student;
    private Course courseA;
    private Course courseB;
    private Course courseC;
    private Course courseD;
    private Course courseE;
    private Course targetCourse;

    @BeforeEach
    void setUp() {
        student = studentRepository.save(new Student("AG123", "Arad", "Ganir", "arad@gmail.com"));

        // Create prerequisite courses
        courseA = courseRepository.save(new Course("CS", "101", "Intro to CS", "Basics", 3));
        courseB = courseRepository.save(new Course("CS", "102", "Programming I", "Programming", 3));
        courseC = courseRepository.save(new Course("MATH", "101", "Calculus I", "Calculus", 4));
        courseD = courseRepository.save(new Course("MATH", "102", "Calculus II", "More Calculus", 4));
        courseE = courseRepository.save(new Course("PHYS", "101", "Physics I", "Physics", 4));

        // Target course that has prerequisites
        targetCourse = courseRepository.save(new Course("CS", "201", "Data Structures", "DS", 3));
    }

    // ==================== isEligibleForCourse ====================

    @Test
    void isEligibleForCourse_noPrerequisites_returnsTrue() {
        // targetCourse has no prerequisites set
        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertTrue(eligible);
    }

    @Test
    void isEligibleForCourse_simpleAnd_allMet_returnsTrue() {
        // Prerequisites: A AND B (must complete both)
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.AND, courseA, courseB);

        // Complete both courses
        completeEnrollment(student, courseA);
        completeEnrollment(student, courseB);

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertTrue(eligible);
    }

    @Test
    void isEligibleForCourse_simpleAnd_someMissing_returnsFalse() {
        // Prerequisites: A AND B (must complete both)
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.AND, courseA, courseB);

        // Only complete A
        completeEnrollment(student, courseA);

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertFalse(eligible);
    }

    @Test
    void isEligibleForCourse_simpleOr_oneMet_returnsTrue() {
        // Prerequisites: A OR B (complete at least one)
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.OR, courseA, courseB);

        // Only complete A
        completeEnrollment(student, courseA);

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertTrue(eligible);
    }

    @Test
    void isEligibleForCourse_simpleOr_noneMet_returnsFalse() {
        // Prerequisites: A OR B (complete at least one)
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.OR, courseA, courseB);

        // Complete neither

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertFalse(eligible);
    }

    @Test
    void isEligibleForCourse_nestedAndOr_satisfied_returnsTrue() {
        // Prerequisites: (A OR B) AND C
        // Must complete C and at least one of A or B

        // Create nested OR group for (A OR B)
        Prerequisite nestedOr = new Prerequisite(null, PrerequisiteType.OR, new HashSet<>());
        nestedOr = prerequisiteRepository.save(nestedOr);
        PrerequisiteItem itemA = new PrerequisiteItem(nestedOr, courseA);
        PrerequisiteItem itemB = new PrerequisiteItem(nestedOr, courseB);
        prerequisiteItemRepository.save(itemA);
        prerequisiteItemRepository.save(itemB);
        nestedOr.getItems().add(itemA);
        nestedOr.getItems().add(itemB);

        // Create root AND: (nested OR) AND C
        Prerequisite rootAnd = new Prerequisite(targetCourse, PrerequisiteType.AND, new HashSet<>());
        rootAnd = prerequisiteRepository.save(rootAnd);
        PrerequisiteItem nestedItem = new PrerequisiteItem(rootAnd, nestedOr);
        PrerequisiteItem itemC = new PrerequisiteItem(rootAnd, courseC);
        prerequisiteItemRepository.save(nestedItem);
        prerequisiteItemRepository.save(itemC);
        rootAnd.getItems().add(nestedItem);
        rootAnd.getItems().add(itemC);

        // Complete A and C (satisfies (A OR B) AND C)
        completeEnrollment(student, courseA);
        completeEnrollment(student, courseC);

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertTrue(eligible);
    }

    @Test
    void isEligibleForCourse_nestedAndOr_notSatisfied_returnsFalse() {
        // Prerequisites: (A OR B) AND C
        // Must complete C and at least one of A or B

        // Create nested OR group for (A OR B)
        Prerequisite nestedOr = new Prerequisite(null, PrerequisiteType.OR, new HashSet<>());
        nestedOr = prerequisiteRepository.save(nestedOr);
        PrerequisiteItem itemA = new PrerequisiteItem(nestedOr, courseA);
        PrerequisiteItem itemB = new PrerequisiteItem(nestedOr, courseB);
        prerequisiteItemRepository.save(itemA);
        prerequisiteItemRepository.save(itemB);
        nestedOr.getItems().add(itemA);
        nestedOr.getItems().add(itemB);

        // Create root AND: (nested OR) AND C
        Prerequisite rootAnd = new Prerequisite(targetCourse, PrerequisiteType.AND, new HashSet<>());
        rootAnd = prerequisiteRepository.save(rootAnd);
        PrerequisiteItem nestedItem = new PrerequisiteItem(rootAnd, nestedOr);
        PrerequisiteItem itemC = new PrerequisiteItem(rootAnd, courseC);
        prerequisiteItemRepository.save(nestedItem);
        prerequisiteItemRepository.save(itemC);
        rootAnd.getItems().add(nestedItem);
        rootAnd.getItems().add(itemC);

        // Only complete A (missing C)
        completeEnrollment(student, courseA);

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertFalse(eligible);
    }

    @Test
    void isEligibleForCourse_deeplyNested_evaluatesCorrectly() {
        // Prerequisites: (A AND B) OR (C AND D)
        // Must complete (A and B) OR (C and D)

        // Create nested AND group for (A AND B)
        Prerequisite nestedAnd1 = new Prerequisite(null, PrerequisiteType.AND, new HashSet<>());
        nestedAnd1 = prerequisiteRepository.save(nestedAnd1);
        PrerequisiteItem itemA = new PrerequisiteItem(nestedAnd1, courseA);
        PrerequisiteItem itemB = new PrerequisiteItem(nestedAnd1, courseB);
        prerequisiteItemRepository.save(itemA);
        prerequisiteItemRepository.save(itemB);
        nestedAnd1.getItems().add(itemA);
        nestedAnd1.getItems().add(itemB);

        // Create nested AND group for (C AND D)
        Prerequisite nestedAnd2 = new Prerequisite(null, PrerequisiteType.AND, new HashSet<>());
        nestedAnd2 = prerequisiteRepository.save(nestedAnd2);
        PrerequisiteItem itemC = new PrerequisiteItem(nestedAnd2, courseC);
        PrerequisiteItem itemD = new PrerequisiteItem(nestedAnd2, courseD);
        prerequisiteItemRepository.save(itemC);
        prerequisiteItemRepository.save(itemD);
        nestedAnd2.getItems().add(itemC);
        nestedAnd2.getItems().add(itemD);

        // Create root OR: (A AND B) OR (C AND D)
        Prerequisite rootOr = new Prerequisite(targetCourse, PrerequisiteType.OR, new HashSet<>());
        rootOr = prerequisiteRepository.save(rootOr);
        PrerequisiteItem nested1Item = new PrerequisiteItem(rootOr, nestedAnd1);
        PrerequisiteItem nested2Item = new PrerequisiteItem(rootOr, nestedAnd2);
        prerequisiteItemRepository.save(nested1Item);
        prerequisiteItemRepository.save(nested2Item);
        rootOr.getItems().add(nested1Item);
        rootOr.getItems().add(nested2Item);

        // Complete C and D (satisfies second branch)
        completeEnrollment(student, courseC);
        completeEnrollment(student, courseD);

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertTrue(eligible);
    }

    @Test
    void isEligibleForCourse_inProgressDoesNotCount_returnsFalse() {
        // Prerequisites: A (must complete)
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.AND, courseA);

        // Enroll but don't complete (IN_PROGRESS)
        enrollmentService.enrollStudent(student.getId(), courseA.getId(), "Fall 2024");

        boolean eligible = eligibilityService.isEligibleForCourse(student.getId(), targetCourse.getId());

        assertFalse(eligible);
    }

    // ==================== getMissingPrerequisites ====================

    @Test
    void getMissingPrerequisites_returnsMissingCourses() {
        // Prerequisites: A AND B
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.AND, courseA, courseB);

        // Complete only A
        completeEnrollment(student, courseA);

        List<Course> missing = eligibilityService.getMissingPrerequisites(student.getId(), targetCourse.getId());

        assertEquals(1, missing.size());
        assertEquals(courseB.getId(), missing.get(0).getId());
    }

    @Test
    void getMissingPrerequisites_nested_returnsAllMissing() {
        // Prerequisites: (A OR B) AND C
        // Create nested OR group for (A OR B)
        Prerequisite nestedOr = new Prerequisite(null, PrerequisiteType.OR, new HashSet<>());
        nestedOr = prerequisiteRepository.save(nestedOr);
        PrerequisiteItem itemA = new PrerequisiteItem(nestedOr, courseA);
        PrerequisiteItem itemB = new PrerequisiteItem(nestedOr, courseB);
        prerequisiteItemRepository.save(itemA);
        prerequisiteItemRepository.save(itemB);
        nestedOr.getItems().add(itemA);
        nestedOr.getItems().add(itemB);

        // Create root AND: (nested OR) AND C
        Prerequisite rootAnd = new Prerequisite(targetCourse, PrerequisiteType.AND, new HashSet<>());
        rootAnd = prerequisiteRepository.save(rootAnd);
        PrerequisiteItem nestedItem = new PrerequisiteItem(rootAnd, nestedOr);
        PrerequisiteItem itemC = new PrerequisiteItem(rootAnd, courseC);
        prerequisiteItemRepository.save(nestedItem);
        prerequisiteItemRepository.save(itemC);
        rootAnd.getItems().add(nestedItem);
        rootAnd.getItems().add(itemC);

        // Complete nothing
        List<Course> missing = eligibilityService.getMissingPrerequisites(student.getId(), targetCourse.getId());

        // Should return A, B (from OR group, since none satisfied), and C
        assertEquals(3, missing.size());
    }

    @Test
    void getMissingPrerequisites_allMet_returnsEmptyList() {
        // Prerequisites: A AND B
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.AND, courseA, courseB);

        // Complete both
        completeEnrollment(student, courseA);
        completeEnrollment(student, courseB);

        List<Course> missing = eligibilityService.getMissingPrerequisites(student.getId(), targetCourse.getId());

        assertTrue(missing.isEmpty());
    }

    // ==================== getEligibleCourses ====================

    @Test
    void getEligibleCourses_returnsCoursesStudentCanTake() {
        // courseA has no prerequisites
        // targetCourse requires courseA
        Prerequisite prereq = createPrerequisite(targetCourse, PrerequisiteType.AND, courseA);

        // Student hasn't completed anything
        List<Course> eligible = eligibilityService.getEligibleCourses(student.getId());

        // Should include courseA, courseB, courseC, courseD, courseE (no prereqs)
        // Should NOT include targetCourse (requires courseA)
        assertTrue(eligible.stream().anyMatch(c -> c.getId().equals(courseA.getId())));
        assertFalse(eligible.stream().anyMatch(c -> c.getId().equals(targetCourse.getId())));
    }

    @Test
    void getEligibleCourses_excludesAlreadyCompleted() {
        // Complete courseA
        completeEnrollment(student, courseA);

        List<Course> eligible = eligibilityService.getEligibleCourses(student.getId());

        // Should NOT include courseA (already completed)
        assertFalse(eligible.stream().anyMatch(c -> c.getId().equals(courseA.getId())));
    }

    // ==================== Helper Methods ====================

    /**
     * Create a simple prerequisite with leaf courses
     */
    private Prerequisite createPrerequisite(Course forCourse, PrerequisiteType type, Course... requiredCourses) {
        Prerequisite prereq = new Prerequisite(forCourse, type, new HashSet<>());
        prereq = prerequisiteRepository.save(prereq);

        for (Course required : requiredCourses) {
            PrerequisiteItem item = new PrerequisiteItem(prereq, required);
            prerequisiteItemRepository.save(item);
            prereq.getItems().add(item);
        }

        return prereq;
    }

    /**
     * Enroll student and mark as completed
     */
    private void completeEnrollment(Student student, Course course) {
        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId(), "Fall 2024");
        enrollmentService.updateEnrollment(enrollment.getId(), Grade.A, EnrollmentStatus.COMPLETED);
    }
}
