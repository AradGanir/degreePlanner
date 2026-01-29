package com.example.degreePlanner.controller;

import com.example.degreePlanner.dto.request.CreateStudentRequest;
import com.example.degreePlanner.dto.response.StudentResponse;
import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.service.StudentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    public StudentController(StudentService studentService) {
        this.studentService = studentService;
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
}
