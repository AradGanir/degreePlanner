package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Requirement;
import com.example.degreePlanner.service.RequirementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public class RequirementController {

    private final RequirementService requirementService;

    public RequirementController(RequirementService requirementService) {
        this.requirementService = requirementService;
    }

    @PostMapping("/majors/{code}/{designation}/requirements")
    public ResponseEntity<Requirement> addRequirement(@PathVariable String code, @PathVariable String designation, @RequestBody Requirement requirement) {
        Requirement created = requirementService.createRequirement(code, designation, requirement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/requirements/{id}")
    public ResponseEntity<Requirement> getRequirement(@PathVariable Long id) {
        return ResponseEntity.ok(requirementService.getRequirementById(id));
    }

    @PutMapping("/requirements/{id}")
    public ResponseEntity<Requirement> updateRequirement(@PathVariable Long id, @RequestBody Requirement requirement) {
        return ResponseEntity.ok(requirementService.updateRequirement(id, requirement));
    }

    @DeleteMapping("/requirements/{id}")
    public ResponseEntity<Void> deleteRequirement(@PathVariable Long id) {
        requirementService.deleteRequirement(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/requirements/{id}/courses/{courseId}")
    public ResponseEntity<Void> addCourseToRequirement(@PathVariable Long id, @PathVariable Long courseId) {
        requirementService.addCourseToRequirement(id, courseId);
        return  ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/requirements/{id}/courses/{courseId}")
    public ResponseEntity<Void> deleteCourseFromRequirement(@PathVariable Long id, @PathVariable Long courseId) {
        requirementService.removeCourseFromRequirement(id, courseId);
        return ResponseEntity.noContent().build();
    }

   @GetMapping("/majors/{code}/{designation}/requirements")
    public ResponseEntity<List<Requirement>> getRequirementsByMajor(@PathVariable String code, @PathVariable String designation){
        return ResponseEntity.ok(requirementService.getRequirementsByMajor(code, designation));
   }
}
