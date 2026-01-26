package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Requirement;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.MajorRepository;
import com.example.degreePlanner.repository.RequirementRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RequirementService {

    private final RequirementRepository requirementRepository;
    private final MajorRepository majorRepository;
    private final CourseRepository courseRepository;


    // Sets the service to talk to the repositories given for each
    public RequirementService(RequirementRepository requirementRepository,
                              MajorRepository majorRepository,
                              CourseRepository courseRepository) {
        this.requirementRepository = requirementRepository;
        this.majorRepository = majorRepository;
        this.courseRepository = courseRepository;
    }


    public Requirement createRequirement(String code, String designation, Requirement requirement) {
        Major major = majorRepository.findByCodeAndDesignation(code, designation)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with identifier " + code + "_" + designation));

        requirement.setMajor(major);
        return requirementRepository.save(requirement);
    }

    public List<Requirement> getRequirementsByMajor(String code, String designation) {
        majorRepository.findByCodeAndDesignation(code, designation)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with identifier " + code + "_" + designation));

        return requirementRepository.findByMajorCodeAndMajorDesignation(code, designation);
    }

    public Requirement getRequirementById(Long id) {
        return requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + id));
    }

    public Requirement updateRequirement(Long id, Requirement updated) {
        Requirement existing = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + id));

        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setMinCredits(updated.getMinCredits());
        existing.setDescription(updated.getDescription());

        return requirementRepository.save(existing);
    }

    public void deleteRequirement(Long id) {
        Requirement requirement = requirementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + id));

        requirementRepository.delete(requirement);
    }

    public void addCourseToRequirement(Long requirementId, Long courseId) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + requirementId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        requirement.getCourses().add(course);
        requirementRepository.save(requirement);
    }

    public void removeCourseFromRequirement(Long requirementId, Long courseId) {
        Requirement requirement = requirementRepository.findById(requirementId)
                .orElseThrow(() -> new ResourceNotFoundException("Requirement not found with id " + requirementId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with id " + courseId));

        requirement.getCourses().remove(course);
        requirementRepository.save(requirement);
    }
}
