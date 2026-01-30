package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Enrollment;
import com.example.degreePlanner.entity.EnrollmentStatus;
import com.example.degreePlanner.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    boolean existsByStudentAndCourseAndSemester(Student student, Course course, String semester);

    List<Enrollment> findByStudent(Student student);

    List<Enrollment> findByStudentAndSemester(Student student, String semester);

    List<Enrollment> findByStudentAndEnrollmentStatus(Student student, EnrollmentStatus status);
}
