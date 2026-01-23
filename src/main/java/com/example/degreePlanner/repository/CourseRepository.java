package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findCourseByCode(String code);

    Course findCourseById(Long id);

    Boolean existsByCodeAndCourseNum(String code, int num);

}