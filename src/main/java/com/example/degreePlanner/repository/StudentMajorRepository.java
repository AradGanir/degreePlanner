package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.entity.StudentMajor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentMajorRepository extends JpaRepository<StudentMajor, Long> {
    boolean existsByStudentAndMajor(Student student, Major major);


    List<StudentMajor> findByStudent(Student student);

    long countByStudent(Student student);

    Optional<StudentMajor> findByStudentAndMajor(Student student, Major major);

    @Modifying
    @Query("UPDATE StudentMajor sm SET sm.isPrimary = false WHERE sm.student = :student AND sm.isPrimary = true")
    void clearPrimaryForStudent(@Param("student") Student student);
}


