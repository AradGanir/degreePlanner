package com.example.degreePlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.degreePlanner.entity.Student;


import java.util.Optional;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentId(String studentId);
    boolean existsByStudentId(String studentId);
    boolean existsByEmail(String email);
    boolean existsByStudentIdAndIdNot(String studentId, Long id);
    boolean existsByEmailAndIdNot(String email, Long id);


}
