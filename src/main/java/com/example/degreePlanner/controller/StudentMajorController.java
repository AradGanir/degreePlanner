package com.example.degreePlanner.controller;


import com.example.degreePlanner.dto.request.Student.DeclareMajorRequest;
import com.example.degreePlanner.dto.response.StudentMajorResponse;
import com.example.degreePlanner.service.StudentMajorService;
import com.example.degreePlanner.entity.StudentMajor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/students/{studentId}/majors")
public class StudentMajorController {

    @Autowired
    private StudentMajorService studentMajorService;

    @PostMapping
    public ResponseEntity<StudentMajor> declareMajor(@PathVariable Long studentId, @RequestBody DeclareMajorRequest declareMajorRequest) {
        StudentMajor declared = studentMajorService.declareMajor(
                studentId,
                declareMajorRequest.getMajorCode(),
                declareMajorRequest.getMajorDesignation(),
                declareMajorRequest.isPrimary()
                );

        return ResponseEntity.status(HttpStatus.CREATED).body(declared);
    }

    @GetMapping
    public ResponseEntity<List<StudentMajorResponse>> getAllMajors(@PathVariable Long studentId) {
        List<StudentMajor> list = studentMajorService.getStudentMajors(studentId);
        List<StudentMajorResponse> responseList = list.stream()
                .map(StudentMajorResponse::fromEntity)
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(responseList);
    }

    @DeleteMapping("/{majorCode}/{majorDesignation}")
    public ResponseEntity<Void> removeMajor(
            @PathVariable Long studentId,
            @PathVariable String majorCode,
            @PathVariable String majorDesignation) {
        studentMajorService.removeMajor(studentId, majorCode, majorDesignation);
        return ResponseEntity.noContent().build();
    }





}
