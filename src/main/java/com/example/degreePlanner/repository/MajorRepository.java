package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {
    boolean existsByCodeAndDesignation(String code, String designation);
    Optional<Major> findByCodeAndDesignation(String code, String designation);
}
