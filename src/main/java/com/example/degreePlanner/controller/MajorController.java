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

    @Autowired
    private final MajorService majorService;

    public MajorController(MajorService majorService) {
        this.majorService = majorService;
    }

    @GetMapping // /majors
    public ResponseEntity<List<Major>> getAllMajors() {
        return ResponseEntity.ok(majorService.getAllMajors());
    }
    @GetMapping("/{codeDesignation}") // /majors/CS_BS
    public ResponseEntity<Major> getMajorByCodeDesignation(@PathVariable String codeDesignation) {
        String[] parts = codeDesignation.split("_");
        String code = parts[0];
        String designation = parts[1];
        return ResponseEntity.ok(majorService.getMajorByCodeAndDesignation(code, designation));
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<Major> getMajorById(@PathVariable Long id) {
        return ResponseEntity.ok(majorService.getMajorById(id));
    }



    @PostMapping // /majors // create
    public ResponseEntity<Major> createMajor(@RequestBody Major major) {
        Major created = majorService.createMajor(major);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{codeDesignation}") // /majors/CS_BS
    public ResponseEntity<Major> updateMajorByCodeDesignation(@PathVariable String codeDesignation, @RequestBody Major major) {
        String[] parts = codeDesignation.split("_");
        String code = parts[0];
        String designation = parts[1];
        return ResponseEntity.ok(majorService.updateMajorByCodeAndDesignation(code, designation, major));
    }

    @PutMapping("/id/{id}")
    public ResponseEntity<Major> updateMajorById(@PathVariable Long id, @RequestBody Major major) {
        return ResponseEntity.ok(majorService.updateMajorById(id, major));
    }



    @DeleteMapping("/{codeDesignation}") // /majors/CS_BS
    public ResponseEntity<Void> deleteMajor(@PathVariable String codeDesignation) {
        String[] parts = codeDesignation.split("_");
        String code = parts[0];
        String designation = parts[1];
        majorService.deleteMajorByCodeAndDesignation(code, designation);
        return ResponseEntity.noContent().build();
    }


}
