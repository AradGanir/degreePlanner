package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.Requirement;
import com.example.degreePlanner.entity.RequirementType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RequirementRepository extends JpaRepository<Requirement, Long> {

    // Find by major's code and designation (navigates the relationship)
    List<Requirement> findByMajorCodeAndMajorDesignation(String code, String designation);

    // Find by major and type
    List<Requirement> findByMajorCodeAndMajorDesignationAndType(String code, String designation, RequirementType type);

    // Check if exists
    boolean existsByMajorCodeAndMajorDesignationAndName(String code, String designation, String name);
}
