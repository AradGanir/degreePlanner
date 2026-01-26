package com.example.degreePlanner.controller;


import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.service.CourseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<Course> getCourseByCodeAndCourseNum(@PathVariable String code, @PathVariable int courseNum){
        return ResponseEntity.ok(courseService.getCourseByCodeAndCourseNum(code, courseNum));
    }

    @PutMapping("/{code}/{courseNum}")
    public ResponseEntity<Course> updateCourseByCodeAndCourseNum(@PathVariable String code, @PathVariable int courseNum, @RequestBody Course course){
        return ResponseEntity.ok(courseService.updateCourseByCodeAndCourseNum(code, courseNum, course));
    }

    @DeleteMapping("/{code}/{courseNum}")
    public ResponseEntity<Void> deleteCourseByCodeAndCourseNum(@PathVariable String code, @PathVariable int courseNum){
        courseService.deleteCourseByCodeAndCourseNum(code, courseNum);
        return ResponseEntity.noContent().build();
    }









}
