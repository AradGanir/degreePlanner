package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Long> {
    Optional<Major> findByCode(String code);
    boolean existsByCode(String code);
    boolean existsByName(String name);
}
