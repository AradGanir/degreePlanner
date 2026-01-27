package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findCourseByCode(String code);

    Optional<Course> findCourseById(Long id);

    Boolean existsByCodeAndCourseNum(String code, String num);

    Optional<Course> findCourseByCodeAndCourseNum(String code, String num);

}