package com.example.degreePlanner.repository;

import com.example.degreePlanner.entity.PrerequisiteItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrerequisiteItemRepository extends JpaRepository<PrerequisiteItem, Long> {
    List<PrerequisiteItem> findByPrerequisiteId(Long prerequisiteId);
}
