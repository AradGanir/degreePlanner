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
    private MajorService majorService;

    @GetMapping // /majors
    public ResponseEntity<List<Major>> getAllMajors() {
        return ResponseEntity.ok(majorService.getAllMajors());
    }
    @GetMapping("/by-code/{code}") // /majors/{code}
    public ResponseEntity<Major> getMajorByCode(@PathVariable String code) {
        return ResponseEntity.ok(majorService.getMajorByCode(code));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Major> getMajorById(@PathVariable Long id) {
        return ResponseEntity.ok(majorService.getMajorById(id));
    }



    @PostMapping // /majors // create
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Major> createMajor(@RequestBody Major major) {
        Major created = majorService.createMajor(major);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    //@PutMapping // /majors/{id} // update
    @PutMapping("/by-code/{code}")
    public ResponseEntity<Major> updateMajorByCode(@PathVariable String code, @RequestBody Major major) {
        return ResponseEntity.ok(majorService.updateMajorByCode(code, major));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Major> updateMajorById(@PathVariable Long id, @RequestBody Major major) {
        return ResponseEntity.ok(majorService.updateMajorById(id, major));
    }



    //@DeleteMapping // /majors/{id}
    @DeleteMapping("/by-code/{code}")
    public ResponseEntity<Void> deleteMajor (@PathVariable String code) {
        majorService.deleteMajorByCode(code);
        return ResponseEntity.noContent().build();
    }


}
