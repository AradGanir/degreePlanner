package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.*;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.PrerequisiteNotMetException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.EnrollmentRepository;
import com.example.degreePlanner.repository.StudentRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EligibilityService eligibilityService;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             StudentRepository studentRepository,
                             CourseRepository courseRepository,
                             @Lazy EligibilityService eligibilityService) {
        this.enrollmentRepository = enrollmentRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.eligibilityService = eligibilityService;
    }

    public Enrollment enrollStudent(Long studentId, Long courseId, String semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        if (enrollmentRepository.existsByStudentAndCourseAndSemester(student, course, semester)) {
            throw new DuplicateResourceException("Student already enrolled in this course for " + semester);
        }

        // Check if student is eligible (has completed prerequisites)
        if (!eligibilityService.isEligibleForCourse(studentId, courseId)) {
            throw new PrerequisiteNotMetException("Student has not completed prerequisites for " + course.getCode() + " " + course.getCourseNum());
        }

        Enrollment enrollment = new Enrollment(student, course, semester, EnrollmentStatus.IN_PROGRESS);
        return enrollmentRepository.save(enrollment);
    }

    public Enrollment getEnrollmentById(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id " + enrollmentId));
    }

    public List<Enrollment> getEnrollments(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        return enrollmentRepository.findByStudent(student);
    }

    public List<Enrollment> getEnrollmentsBySemester(Long studentId, String semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        return enrollmentRepository.findByStudentAndSemester(student, semester);
    }

    public List<Enrollment> getEnrollmentsByStatus(Long studentId, EnrollmentStatus status) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        return enrollmentRepository.findByStudentAndEnrollmentStatus(student, status);
    }

    public Enrollment updateEnrollment(Long enrollmentId, Grade grade, EnrollmentStatus status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id " + enrollmentId));

        enrollment.setGrade(grade);
        enrollment.setEnrollmentStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    public void removeEnrollment(Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found with id " + enrollmentId));

        enrollmentRepository.delete(enrollment);
    }

    public List<Course> getCompletedCourses(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        return enrollmentRepository.findByStudentAndEnrollmentStatus(student, EnrollmentStatus.COMPLETED)
                .stream()
                .map(Enrollment::getCourse)
                .toList();
    }

    public List<Course> getInProgressCourses(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        return enrollmentRepository.findByStudentAndEnrollmentStatus(student, EnrollmentStatus.IN_PROGRESS)
                .stream()
                .map(Enrollment::getCourse)
                .toList();
    }

    public boolean isEnrolled(Long studentId, Long courseId, String semester) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        return enrollmentRepository.existsByStudentAndCourseAndSemester(student, course, semester);
    }
}
