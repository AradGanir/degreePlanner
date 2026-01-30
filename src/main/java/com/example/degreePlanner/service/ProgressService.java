package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class ProgressService {

    private final StudentRepository studentRepository;
    private final StudentMajorRepository studentMajorRepository;
    private final MajorRepository majorRepository;
    private final RequirementRepository requirementRepository;
    private final EnrollmentService enrollmentService;

    public ProgressService(StudentRepository studentRepository,
                           StudentMajorRepository studentMajorRepository,
                           MajorRepository majorRepository,
                           RequirementRepository requirementRepository,
                           EnrollmentService enrollmentService) {
        this.studentRepository = studentRepository;
        this.studentMajorRepository = studentMajorRepository;
        this.majorRepository = majorRepository;
        this.requirementRepository = requirementRepository;
        this.enrollmentService = enrollmentService;
    }

    /**
     * Result record for major progress information.
     */
    public record MajorProgress(
            Major major,
            int totalCreditsRequired,
            int creditsCompleted,
            int creditsRemaining,
            double percentComplete,
            List<RequirementProgress> requirements
    ) {}

    /**
     * Result record for requirement progress information.
     */
    public record RequirementProgress(
            Requirement requirement,
            RequirementStatus status,
            List<Course> completedCourses,
            List<Course> remainingCourses,
            int creditsCompleted,
            int creditsRemaining
    ) {}

    /**
     * Get progress for all declared majors for a student.
     */
    public List<MajorProgress> getOverallProgress(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        List<StudentMajor> studentMajors = studentMajorRepository.findByStudent(student);
        List<MajorProgress> progressList = new ArrayList<>();

        for (StudentMajor sm : studentMajors) {
            Major major = sm.getMajor();
            progressList.add(getMajorProgressInternal(studentId, major));
        }

        return progressList;
    }

    /**
     * Get progress for a specific major for a student.
     */
    public MajorProgress getMajorProgress(Long studentId, String majorCode, String majorDesignation) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Major major = majorRepository.findByCodeAndDesignation(majorCode, majorDesignation)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found: " + majorCode + "_" + majorDesignation));

        // Verify student has declared this major
        if (!studentMajorRepository.existsByStudentAndMajor(student, major)) {
            throw new ResourceNotFoundException("Student has not declared major: " + majorCode + "_" + majorDesignation);
        }

        return getMajorProgressInternal(studentId, major);
    }

    /**
     * Get progress for a single requirement.
     */
    public RequirementProgress getRequirementProgress(Long studentId, Long requirementId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + requirementId));

        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        return calculateRequirementProgress(requirement, completedCourses);
    }

    /**
     * Calculate total credits completed toward a major.
     * Only counts courses that are part of the major's requirements.
     */
    public int calculateCreditsCompleted(Long studentId, Long majorId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + majorId));

        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        List<Requirement> requirements = requirementRepository.findByMajorCodeAndMajorDesignation(
                major.getCode(), major.getDesignation());

        // Collect all courses that count toward this major
        Set<Long> majorCourseIds = requirements.stream()
                .flatMap(r -> r.getCourses().stream())
                .map(Course::getId)
                .collect(java.util.stream.Collectors.toSet());

        // Sum credits for completed courses that are in the major
        return completedCourses.stream()
                .filter(c -> majorCourseIds.contains(c.getId()))
                .mapToInt(Course::getCredits)
                .sum();
    }

    /**
     * Get list of completed requirements for a major.
     */
    public List<Requirement> getCompletedRequirements(Long studentId, Long majorId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + majorId));

        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        List<Requirement> requirements = requirementRepository.findByMajorCodeAndMajorDesignation(
                major.getCode(), major.getDesignation());

        return requirements.stream()
                .filter(r -> calculateRequirementProgress(r, completedCourses).status() == RequirementStatus.COMPLETE)
                .toList();
    }

    /**
     * Get list of unfulfilled requirements for a major.
     */
    public List<Requirement> getRemainingRequirements(Long studentId, Long majorId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Major major = majorRepository.findById(majorId)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + majorId));

        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        List<Requirement> requirements = requirementRepository.findByMajorCodeAndMajorDesignation(
                major.getCode(), major.getDesignation());

        return requirements.stream()
                .filter(r -> calculateRequirementProgress(r, completedCourses).status() != RequirementStatus.COMPLETE)
                .toList();
    }

    /**
     * Internal method to calculate major progress.
     */
    private MajorProgress getMajorProgressInternal(Long studentId, Major major) {
        List<Course> completedCourses = enrollmentService.getCompletedCourses(studentId);
        List<Requirement> requirements = requirementRepository.findByMajorCodeAndMajorDesignation(
                major.getCode(), major.getDesignation());

        // Calculate requirement progress for each requirement
        List<RequirementProgress> requirementProgressList = requirements.stream()
                .map(r -> calculateRequirementProgress(r, completedCourses))
                .toList();

        // Calculate total credits completed toward this major
        int creditsCompleted = calculateCreditsCompletedInternal(completedCourses, requirements);
        int totalCreditsRequired = major.getTotalCreditsRequired();
        int creditsRemaining = Math.max(0, totalCreditsRequired - creditsCompleted);
        double percentComplete = totalCreditsRequired > 0
                ? (double) creditsCompleted / totalCreditsRequired * 100.0
                : 0.0;

        return new MajorProgress(
                major,
                totalCreditsRequired,
                creditsCompleted,
                creditsRemaining,
                percentComplete,
                requirementProgressList
        );
    }

    /**
     * Calculate credits completed from courses that count toward the major's requirements.
     */
    private int calculateCreditsCompletedInternal(List<Course> completedCourses, List<Requirement> requirements) {
        // Collect all unique course IDs from all requirements
        Set<Long> majorCourseIds = requirements.stream()
                .flatMap(r -> r.getCourses().stream())
                .map(Course::getId)
                .collect(java.util.stream.Collectors.toSet());

        // Sum credits for completed courses that are in the major
        return completedCourses.stream()
                .filter(c -> majorCourseIds.contains(c.getId()))
                .mapToInt(Course::getCredits)
                .sum();
    }

    /**
     * Calculate progress for a single requirement.
     *
     * Logic:
     * - If minCredits is set: student needs at least that many credits from the requirement's courses
     * - If minCredits is null: student needs to complete ALL courses in the requirement
     */
    private RequirementProgress calculateRequirementProgress(Requirement requirement, List<Course> completedCourses) {
        Set<Course> requirementCourses = requirement.getCourses();

        // Find which of the requirement's courses have been completed
        List<Course> completed = requirementCourses.stream()
                .filter(rc -> completedCourses.stream().anyMatch(cc -> cc.getId().equals(rc.getId())))
                .toList();

        // Find remaining courses
        List<Course> remaining = requirementCourses.stream()
                .filter(rc -> completedCourses.stream().noneMatch(cc -> cc.getId().equals(rc.getId())))
                .toList();

        int creditsCompleted = completed.stream().mapToInt(Course::getCredits).sum();
        int totalRequirementCredits = requirementCourses.stream().mapToInt(Course::getCredits).sum();

        // Determine status based on requirement type
        RequirementStatus status;
        int creditsRemaining;

        Integer minCredits = requirement.getMinCredits();

        if (minCredits != null) {
            // Credit-based requirement: need at least minCredits from the course list
            creditsRemaining = Math.max(0, minCredits - creditsCompleted);

            if (creditsCompleted >= minCredits) {
                status = RequirementStatus.COMPLETE;
            } else if (creditsCompleted > 0) {
                status = RequirementStatus.IN_PROGRESS;
            } else {
                status = RequirementStatus.NOT_STARTED;
            }
        } else {
            // Course-based requirement: need all courses
            creditsRemaining = remaining.stream().mapToInt(Course::getCredits).sum();

            if (remaining.isEmpty()) {
                status = RequirementStatus.COMPLETE;
            } else if (!completed.isEmpty()) {
                status = RequirementStatus.IN_PROGRESS;
            } else {
                status = RequirementStatus.NOT_STARTED;
            }
        }

        return new RequirementProgress(
                requirement,
                status,
                completed,
                remaining,
                creditsCompleted,
                creditsRemaining
        );
    }
}
