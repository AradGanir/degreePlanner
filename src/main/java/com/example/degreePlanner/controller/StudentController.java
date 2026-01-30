package com.example.degreePlanner.controller;

import com.example.degreePlanner.dto.request.CreateStudentRequest;
import com.example.degreePlanner.dto.response.EligibilityResponse;
import com.example.degreePlanner.dto.response.MajorProgressResponse;
import com.example.degreePlanner.dto.response.StudentResponse;
import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.service.CourseService;
import com.example.degreePlanner.service.EligibilityService;
import com.example.degreePlanner.service.ProgressService;
import com.example.degreePlanner.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    private final EligibilityService eligibilityService;
    private final CourseService courseService;
    private final ProgressService progressService;

    public StudentController(StudentService studentService,
                             EligibilityService eligibilityService,
                             CourseService courseService,
                             ProgressService progressService) {
        this.studentService = studentService;
        this.eligibilityService = eligibilityService;
        this.courseService = courseService;
        this.progressService = progressService;
    }

    @GetMapping()
    public ResponseEntity<List<Student>> getAllStudents() {
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Student> getStudentById(@PathVariable("id") Long studentId) {
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StudentResponse createStudent(@RequestBody CreateStudentRequest request) {
        Student student = new Student (
                request.getStudentId(),
                request.getFirstName(),
                request.getLastName(),
                request.getEmail()
        );

        Student saved = studentService.createStudent(student);
        return StudentResponse.fromEntity(saved);
    }


    @PutMapping("/{id}")
    public ResponseEntity<Student> updateStudent(@PathVariable("id") Long studentId, @RequestBody Student student) {
        return  ResponseEntity.ok(studentService.updateStudent(studentId, student));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Student> deleteStudent(@PathVariable("id") Long studentId) {
        studentService.deleteStudent(studentId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/eligibility/{courseCode}/{courseNum}")
    public ResponseEntity<EligibilityResponse> checkEligibility(
            @PathVariable("id") Long studentId,
            @PathVariable String courseCode,
            @PathVariable String courseNum) {

        Course course = courseService.getCourseByCodeAndCourseNum(courseCode, courseNum);
        boolean eligible = eligibilityService.isEligibleForCourse(studentId, course.getId());
        List<Course> missing = eligibilityService.getMissingPrerequisites(studentId, course.getId());

        return ResponseEntity.ok(EligibilityResponse.fromCourse(course, eligible, missing));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<List<MajorProgressResponse>> getOverallProgress(
            @PathVariable("id") Long studentId) {

        List<MajorProgressResponse> progress = progressService.getOverallProgress(studentId).stream()
                .map(MajorProgressResponse::fromServiceResult)
                .toList();

        return ResponseEntity.ok(progress);
    }

    @GetMapping("/{id}/progress/{code}/{designation}")
    public ResponseEntity<MajorProgressResponse> getMajorProgress(
            @PathVariable("id") Long studentId,
            @PathVariable String code,
            @PathVariable String designation) {

        ProgressService.MajorProgress progress = progressService.getMajorProgress(studentId, code, designation);
        return ResponseEntity.ok(MajorProgressResponse.fromServiceResult(progress));
    }
}
