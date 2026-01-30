package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.PrerequisiteRepository;
import com.example.degreePlanner.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class EligibilityService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    private final EnrollmentService enrollmentService;

    public EligibilityService(StudentRepository studentRepository,
                              CourseRepository courseRepository,
                              PrerequisiteRepository prerequisiteRepository,
                              EnrollmentService enrollmentService) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Check if a student is eligible to take a course (has completed all prerequisites)
     */
    public boolean isEligibleForCourse(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        // Get completed courses for this student
        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);

        // Get prerequisite for this course
        Optional<Prerequisite> prereqOpt = prerequisiteRepository.findByCourseId(courseId);

        // No prerequisites = eligible
        if (prereqOpt.isEmpty()) {
            return true;
        }

        return checkPrerequisite(completedCourses, prereqOpt.get());
    }

    /**
     * Get list of missing prerequisite courses for a student
     */
    public List<Course> getMissingPrerequisites(Long studentId, Long courseId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        List<Course> missingCourses = new ArrayList<>();

        Optional<Prerequisite> prereqOpt = prerequisiteRepository.findByCourseId(courseId);

        if (prereqOpt.isEmpty()) {
            return missingCourses; // No prerequisites, nothing missing
        }

        collectMissingPrerequisites(completedCourses, prereqOpt.get(), missingCourses);
        return missingCourses;
    }

    /**
     * Get all courses the student is eligible to take
     */
    public List<Course> getEligibleCourses(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        List<Course> inProgressCourses = enrollmentService.getInProgressCourses(studentId);
        List<Course> allCourses = courseRepository.findAll();

        List<Course> eligibleCourses = new ArrayList<>();

        for (Course course : allCourses) {
            // Skip already completed or in-progress courses
            if (completedCourses.contains(course) || inProgressCourses.contains(course)) {
                continue;
            }

            // Check if eligible
            Optional<Prerequisite> prereqOpt = prerequisiteRepository.findByCourseId(course.getId());

            if (prereqOpt.isEmpty()) {
                // No prerequisites, eligible
                eligibleCourses.add(course);
            } else if (checkPrerequisite(completedCourses, prereqOpt.get())) {
                eligibleCourses.add(course);
            }
        }

        return eligibleCourses;
    }

    /**
     * Recursive check for prerequisite satisfaction
     * AND type: ALL items must be satisfied
     * OR type: ANY item must be satisfied
     */
    private boolean checkPrerequisite(List<Course> completedCourses, Prerequisite prereq) {
        Set<PrerequisiteItem> items = prereq.getItems();

        if (items == null || items.isEmpty()) {
            return true; // No items means satisfied
        }

        if (prereq.getType() == PrerequisiteType.AND) {
            // ALL items must be satisfied
            for (PrerequisiteItem item : items) {
                if (!checkItem(completedCourses, item)) {
                    return false;
                }
            }
            return true;
        } else {
            // OR: ANY item must be satisfied
            for (PrerequisiteItem item : items) {
                if (checkItem(completedCourses, item)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Check a single prerequisite item
     * Leaf: check if student has completed the course
     * Group: recursively check the nested prerequisite
     */
    private boolean checkItem(List<Course> completedCourses, PrerequisiteItem item) {
        if (item.isLeaf()) {
            // Leaf node: check if course is completed
            return completedCourses.stream()
                    .anyMatch(c -> c.getId().equals(item.getCourse().getId()));
        } else if (item.isGroup()) {
            // Nested group: recurse
            return checkPrerequisite(completedCourses, item.getNestedPrerequisite());
        }
        // Neither leaf nor group (shouldn't happen)
        return true;
    }

    /**
     * Recursively collect all missing prerequisite courses
     */
    private void collectMissingPrerequisites(List<Course> completedCourses, Prerequisite prereq, List<Course> missing) {
        Set<PrerequisiteItem> items = prereq.getItems();

        if (items == null || items.isEmpty()) {
            return;
        }

        if (prereq.getType() == PrerequisiteType.AND) {
            // For AND: collect ALL missing items
            for (PrerequisiteItem item : items) {
                collectMissingFromItem(completedCourses, item, missing);
            }
        } else {
            // For OR: only collect if NONE are satisfied
            boolean anySatisfied = items.stream()
                    .anyMatch(item -> checkItem(completedCourses, item));

            if (!anySatisfied) {
                // None satisfied, collect all as options
                for (PrerequisiteItem item : items) {
                    collectMissingFromItem(completedCourses, item, missing);
                }
            }
        }
    }

    /**
     * Collect missing courses from a single item
     */
    private void collectMissingFromItem(List<Course> completedCourses, PrerequisiteItem item, List<Course> missing) {
        if (item.isLeaf()) {
            Course course = item.getCourse();
            boolean completed = completedCourses.stream()
                    .anyMatch(c -> c.getId().equals(course.getId()));
            if (!completed && !missing.contains(course)) {
                missing.add(course);
            }
        } else if (item.isGroup()) {
            collectMissingPrerequisites(completedCourses, item.getNestedPrerequisite(), missing);
        }
    }
}
