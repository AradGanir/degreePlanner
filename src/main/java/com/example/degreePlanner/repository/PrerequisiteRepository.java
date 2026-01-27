package com.example.degreePlanner.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.degreePlanner.entity.Prerequisite;

import java.util.Optional;

public interface PrerequisiteRepository extends JpaRepository<Prerequisite, Long> {
    Optional<Prerequisite> findByCourseId(Long courseId);
    void deleteByCourseId(Long courseId);
}
