package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.repository.CourseRepository;
import org.springframework.stereotype.Service;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    public Course courseExistsByCode(String code) {
        return courseRepository.findCourseByCode(code);
    }
    public Course courseExistsById(Long id) {
        return courseRepository.findCourseById(id);
    }

    public Course createCourse(Course course) {
        if (courseRepository.existsByCodeAndCourseNum(course.getCode(), course.getCourseNum())) {
            throw new DuplicateResourceException("Course with identity " + course.getCode() + course.getCourseNum() + " already exists");
        }
        return courseRepository.save(course);
    }
}
