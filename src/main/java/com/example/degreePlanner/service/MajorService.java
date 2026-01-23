package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.MajorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MajorService {
    private final MajorRepository majorRepository;

    public MajorService(MajorRepository majorRepository) {
        this.majorRepository = majorRepository;
    }

    public Major createMajor(Major major){
        if (majorRepository.existsByCode(major.getCode())) {
            throw new DuplicateResourceException("Major with code " + major.getCode() + " already exists");
        }
        if (majorRepository.existsByName(major.getName())) {
            throw new DuplicateResourceException("Major with name " + major.getName() + " already exists");
        }
        return majorRepository.save(major);
    }

    public Major getMajorById(Long id) {
        return majorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + id));
    }

    public Boolean majorExistsById(Long id) {
        return majorRepository.existsById(id);
    }

    public Major getMajorByCode(String code) {
        return majorRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Major not found with code " + code));
    }

    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    public Major updateMajorByCode(String code, Major updated) {
        Major existing = majorRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Major not found with code " + code));

        // If name is changing
        if (!existing.getName().equals(updated.getName()) && majorRepository.existsByName(updated.getName())) {
            throw new DuplicateResourceException("Major with name " + updated.getName() + " already exists");
        }

        existing.setName(updated.getName());
        existing.setDesignation(updated.getDesignation());
        existing.setTotalCreditsRequired(updated.getTotalCreditsRequired());
        existing.setDescription(updated.getDescription());
        return majorRepository.save(existing);

    }

    public Major updateMajorById(Long id, Major updated) {
        Major existing = majorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + id));
        if (!existing.getName().equals(updated.getName()) && majorRepository.existsByName(updated.getName())) {
            throw new DuplicateResourceException("Major with name " + updated.getName() + " already exists");
        }

        existing.setName(updated.getName());
        existing.setDesignation(updated.getDesignation());
        existing.setTotalCreditsRequired(updated.getTotalCreditsRequired());
        existing.setDescription(updated.getDescription());
        existing.setCode(updated.getCode());

        return majorRepository.save(existing);
    }

    public void deleteMajorByCode(String code) {
        Major major = majorRepository.findByCode(code).orElseThrow(() -> new ResourceNotFoundException("Major not found with code " + code));
        majorRepository.delete(major);
    }






}
