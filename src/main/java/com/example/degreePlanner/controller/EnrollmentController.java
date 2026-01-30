package com.example.degreePlanner.controller;

import com.example.degreePlanner.dto.request.CreateEnrollmentRequest;
import com.example.degreePlanner.dto.request.UpdateEnrollmentRequest;
import com.example.degreePlanner.dto.response.EnrollmentResponse;
import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Enrollment;
import com.example.degreePlanner.service.CourseService;
import com.example.degreePlanner.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students/{studentId}/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final CourseService courseService;

    public EnrollmentController(EnrollmentService enrollmentService, CourseService courseService) {
        this.enrollmentService = enrollmentService;
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<EnrollmentResponse> enrollStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody CreateEnrollmentRequest request) {

        Course course = courseService.getCourseByCodeAndCourseNum(request.getCourseCode(), request.getCourseNum());
        Enrollment enrollment = enrollmentService.enrollStudent(studentId, course.getId(), request.getSemester());

        return ResponseEntity.status(HttpStatus.CREATED).body(EnrollmentResponse.fromEntity(enrollment));
    }

    @GetMapping
    public ResponseEntity<List<EnrollmentResponse>> getEnrollments(@PathVariable Long studentId) {
        List<Enrollment> enrollments = enrollmentService.getEnrollments(studentId);
        List<EnrollmentResponse> response = enrollments.stream()
                .map(EnrollmentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{enrollmentId}")
    public ResponseEntity<EnrollmentResponse> updateEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody UpdateEnrollmentRequest request) {

        Enrollment enrollment = enrollmentService.updateEnrollment(
                enrollmentId,
                request.getGrade(),
                request.getStatus());

        return ResponseEntity.ok(EnrollmentResponse.fromEntity(enrollment));
    }

    @DeleteMapping("/{enrollmentId}")
    public ResponseEntity<Void> removeEnrollment(
            @PathVariable Long studentId,
            @PathVariable Long enrollmentId) {

        enrollmentService.removeEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}
