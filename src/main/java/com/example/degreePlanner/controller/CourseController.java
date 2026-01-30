package com.example.degreePlanner.controller;


import com.example.degreePlanner.dto.request.SetPrerequisitesRequest;
import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Prerequisite;
import com.example.degreePlanner.entity.PrerequisiteType;
import com.example.degreePlanner.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping
    public ResponseEntity<Course>  addCourse(@RequestBody Course course){
        Course created = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping()
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    @GetMapping("/{code}/{courseNum}")
    public ResponseEntity<Course> getCourseByCodeAndCourseNum(@PathVariable String code, @PathVariable String courseNum){
        return ResponseEntity.ok(courseService.getCourseByCodeAndCourseNum(code, courseNum));
    }

    @PutMapping("/{code}/{courseNum}")
    public ResponseEntity<Course> updateCourseByCodeAndCourseNum(@PathVariable String code, @PathVariable String courseNum, @RequestBody Course course){
        return ResponseEntity.ok(courseService.updateCourseByCodeAndCourseNum(code, courseNum, course));
    }

    @DeleteMapping("/{code}/{courseNum}")
    public ResponseEntity<Void> deleteCourseByCodeAndCourseNum(@PathVariable String code, @PathVariable String courseNum){
        courseService.deleteCourseByCodeAndCourseNum(code, courseNum);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{code}/{courseNum}/prerequisite")
    public ResponseEntity<Prerequisite> setPrerequisites(
            @PathVariable String code,
            @PathVariable String courseNum,
            @RequestBody Map<String, Object> body) {
        Course course = courseService.getCourseByCodeAndCourseNum(code, courseNum);

        PrerequisiteType type = PrerequisiteType.valueOf((String) body.get("type"));

        // Cast the courseIds from the JSON array
        @SuppressWarnings("unchecked")
        List<Integer> courseIdsInt = (List<Integer>) body.get("courseIds");
        List<Long> courseIds = courseIdsInt.stream()
                .map(Integer::longValue)
                .toList();

        Prerequisite prereq = courseService.setPrerequisites(course.getId(), type, courseIds);
        return ResponseEntity.ok(prereq);
    }

    @GetMapping("/{code}/{courseNum}/prerequisite")
    public ResponseEntity<Prerequisite> getPrerequisites(@PathVariable String code, @PathVariable String courseNum) {
        Long courseId = courseService.getCourseByCodeAndCourseNum(code, courseNum).getId();
        return ResponseEntity.ok(courseService.getPrerequisite(courseId));
    }

    /**
     * Set complex nested prerequisites.
     * Example body for (A OR B) AND C:
     * {
     *   "type": "AND",
     *   "items": [
     *     { "type": "OR", "items": [{ "courseId": 1 }, { "courseId": 2 }] },
     *     { "courseId": 3 }
     *   ]
     * }
     */
    @PutMapping("/{code}/{courseNum}/prerequisite/nested")
    public ResponseEntity<Prerequisite> setNestedPrerequisites(
            @PathVariable String code,
            @PathVariable String courseNum,
            @Valid @RequestBody SetPrerequisitesRequest request) {
        Course course = courseService.getCourseByCodeAndCourseNum(code, courseNum);
        Prerequisite prereq = courseService.setNestedPrerequisites(course.getId(), request);
        return ResponseEntity.ok(prereq);
    }









}
