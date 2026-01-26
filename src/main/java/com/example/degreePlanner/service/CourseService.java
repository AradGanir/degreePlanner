package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.repository.CourseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.degreePlanner.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course createCourse(Course course) {
        if (courseRepository.existsByCodeAndCourseNum(course.getCode(), course.getCourseNum())) {
            throw new DuplicateResourceException("Course with identity " + course.getCode() + course.getCourseNum() + " already exists");
        }
        return courseRepository.save(course);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
    }

    public List<Course> getCourseByCode(String code) {
        List<Course> courses = courseRepository.findCourseByCode(code);
        if (courses.isEmpty()) {
            throw new ResourceNotFoundException("Course not found with code " + code);
        }
        return courses;
    }

    public Course getCourseByCodeAndCourseNum(String code, int courseNum) {
        return courseRepository.findCourseByCodeAndCourseNum(code, courseNum).orElseThrow(() -> new ResourceNotFoundException("Course " + code+courseNum + " not found"));
    }

    public Course updateCourseById(Long id, Course course) {
        Course existing = courseRepository.findCourseById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));

        existing.setCode(course.getCode());
        existing.setCourseNum(course.getCourseNum());
        existing.setTitle(course.getTitle());
        existing.setDescription(course.getDescription());
        existing.setCredits(course.getCredits());

        return courseRepository.save(existing);

    }

    public Course updateCourseByCodeAndCourseNum(String code, int courseNum, Course course) {
        Course existing = courseRepository.findCourseByCodeAndCourseNum(code, courseNum).orElseThrow(() -> new ResourceNotFoundException("Course " + code+courseNum + " not found"));

        existing.setCode(course.getCode());
        existing.setCourseNum(course.getCourseNum());
        existing.setTitle(course.getTitle());
        existing.setDescription(course.getDescription());
        existing.setCredits(course.getCredits());
        return courseRepository.save(existing);
    }

    public void deleteCourseByCodeAndCourseNum(String code, int courseNum) {
        Course course = courseRepository.findCourseByCodeAndCourseNum(code, courseNum)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + code+courseNum + " not found"));

        courseRepository.delete(course);
    }

    public void deleteCourseById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
        courseRepository.delete(course);
    }
}
