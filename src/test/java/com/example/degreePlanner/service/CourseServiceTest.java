package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.MajorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
public class CourseServiceTest {
    //
    //getAllCourses_returnsList
    //updateCourse_validData_updatesAndReturnsCourse
    //deleteCourse_exists_deletesCourse

    @Autowired
    private CourseService courseService;
    @Autowired
    private MajorRepository majorRepository;

    @Test
    void createCourse_validData_savesCourse() {
        Course course = new Course("MATH", 111, "Calculus", "learn integration and derivation", 3);

        Course savedCourse = courseService.createCourse(course);

        assertThat(savedCourse).isNotNull();
        assertThat(savedCourse.getId()).isNotNull();
        assertThat(savedCourse.getCode()).isEqualTo("MATH");
        assertThat(courseService.courseExistsById(savedCourse.getId()));
        assertThat(courseService.courseExistsByCode("MATH"));
    }

    @Test
    void createCourse_duplicateCode_throwsException() {
        Course course = new Course("MATH", 111, "Calculus", "learn integration and derivation", 3);
        Course course2 = new Course("EON", 111, "Calculus", "learn integration and derivation", 3);
        Course course3 = new Course("MATH", 112, "Calculus", "learn integration and derivation", 3);
        Course invalidCourse = new Course("MATH", 111, "Calculus", "learn integration and derivation", 3);

        course = courseService.createCourse(course);
        course2 = courseService.createCourse(course2);
        course3 = courseService.createCourse(course3);


        assertThat(course).isNotNull();
        assertThat(course2).isNotNull();
        assertThat(course3).isNotNull();
        assertThatThrownBy(() -> courseService.createCourse(invalidCourse)).isInstanceOf(DuplicateResourceException.class).hasMessage("Course with identity MATH111 already exists");
    }

    @Test
    void getCourse_exists_returnsCourse() {
        Course course = new Course("CS", 101, "Intro to CS", "Basics of programming", 3);
        Course savedCourse = courseService.createCourse(course);

        Course byId = courseService.courseExistsById(course.getId());
        Course byCode = courseService.courseExistsByCode(course.getCode());

        assertThat(byId).isNotNull();
        assertThat(byCode).isNotNull();
        assertThat(byId.getId()).isEqualTo(course.getId());
        assertThat(byCode.getCode()).isEqualTo(course.getCode());
        assertThat(byId.getCode()).isEqualTo(savedCourse.getCode());
    }
}
