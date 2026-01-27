package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Prerequisite;
import com.example.degreePlanner.entity.PrerequisiteType;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.repository.CourseRepository;
import org.springframework.stereotype.Service;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.PrerequisiteRepository;
import com.example.degreePlanner.entity.PrerequisiteItem;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@Transactional
public class CourseService {
    private final CourseRepository courseRepository;
    private final PrerequisiteRepository prerequisiteRepository;
    public CourseService(CourseRepository courseRepository,  PrerequisiteRepository prerequisiteRepository) {
        this.courseRepository = courseRepository;
        this.prerequisiteRepository = prerequisiteRepository;
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

    public Course getCourseByCodeAndCourseNum(String code, String courseNum) {
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

    public Course updateCourseByCodeAndCourseNum(String code, String courseNum, Course course) {
        Course existing = courseRepository.findCourseByCodeAndCourseNum(code, courseNum).orElseThrow(() -> new ResourceNotFoundException("Course " + code+courseNum + " not found"));

        existing.setCode(course.getCode());
        existing.setCourseNum(course.getCourseNum());
        existing.setTitle(course.getTitle());
        existing.setDescription(course.getDescription());
        existing.setCredits(course.getCredits());
        return courseRepository.save(existing);
    }

    public void deleteCourseByCodeAndCourseNum(String code, String courseNum) {
        Course course = courseRepository.findCourseByCodeAndCourseNum(code, courseNum)
                .orElseThrow(() -> new ResourceNotFoundException("Course " + code+courseNum + " not found"));

        courseRepository.delete(course);
    }

    public void deleteCourseById(Long id) {
        Course course = courseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + id));
        courseRepository.delete(course);
    }

    public Prerequisite setPrerequisites(Long courseId, PrerequisiteType type, List<Long> requiredCourseIds) {
        // Find course that has prereqs
        Course course = courseRepository.findById(courseId).orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));


        // Find all the required courses for this course
        List<Course> requiredCourses = requiredCourseIds.stream()
                .map(id -> courseRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Required course not found with id " + id)))
                .toList();

        // Delete the existing prerequisite - only one prerequisite allowed, complex prerequisites capture all classes in one
        prerequisiteRepository.findByCourseId(courseId).ifPresent(prerequisiteRepository::delete);

        // Delete the existing one to ensure consistency
        Prerequisite prerequisite = new Prerequisite(course, type, new HashSet<>());
        prerequisiteRepository.flush();


        // Create prerequisite items for each required course
        for (Course requiredCourse : requiredCourses) {
            PrerequisiteItem item = new PrerequisiteItem(prerequisite, requiredCourse);
            prerequisite.getItems().add(item);
        }

        // Save and return
        return prerequisiteRepository.save(prerequisite);
    }

    public Prerequisite getPrerequisite(Long id) {
        if (courseRepository.findById(id).isEmpty()) {
            throw new ResourceNotFoundException("Course not found with id " + id);
        }

        return prerequisiteRepository.findByCourseId(id).orElse(null);
    }

    public void removePrerequisite(Long courseId) {
        prerequisiteRepository.findByCourseId(courseId).ifPresent(prerequisiteRepository::delete);
    }







}
