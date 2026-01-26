package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/majors")
public class MajorController {

    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping
    public ResponseEntity<List<Major>> getAllMajors() {
        return ResponseEntity.ok(majorService.getAllMajors());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Major> getMajorById(@PathVariable Long id) {
        return ResponseEntity.ok(majorService.getMajorById(id));
    }

    @GetMapping("/{code}/{designation}")
    public ResponseEntity<Major> getMajorByCodeAndDesignation(
            @PathVariable String code,
            @PathVariable String designation) {
        return ResponseEntity.ok(majorService.getMajorByCodeAndDesignation(code, designation));
    }

    @PostMapping
    public ResponseEntity<Major> createMajor(@RequestBody Major major) {
        Major created = majorService.createMajor(major);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{code}/{designation}")
    public ResponseEntity<Major> updateMajor(
            @PathVariable String code,
            @PathVariable String designation,
            @RequestBody Major major) {
        return ResponseEntity.ok(majorService.updateMajorByCodeAndDesignation(code, designation, major));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Major> updateMajorById(@PathVariable Long id, @RequestBody Major major) {
        return ResponseEntity.ok(majorService.updateMajorById(id, major));
    }

    @DeleteMapping("/{code}/{designation}")
    public ResponseEntity<Void> deleteMajor(
            @PathVariable String code,
            @PathVariable String designation) {
        majorService.deleteMajorByCodeAndDesignation(code, designation);
        return ResponseEntity.noContent().build();
    }

}
