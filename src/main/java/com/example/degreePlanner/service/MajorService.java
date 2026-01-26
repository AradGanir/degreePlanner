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
        if (majorRepository.existsByCodeAndDesignation(major.getCode(),major.getDesignation())) {
            throw new DuplicateResourceException("Major with code " + major.getCode() + " and designation " + major.getDesignation() + " already exists");
        }
        return majorRepository.save(major);
    }

    public Major getMajorById(Long id) {
        return majorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + id));
    }

    public Boolean majorExistsById(Long id) {
        return majorRepository.existsById(id);
    }

    public Major getMajorByCodeAndDesignation(String code, String designation) {
        return majorRepository.findByCodeAndDesignation(code, designation)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with identifier " + code + "_" + designation));
    }

    public List<Major> getAllMajors() {
        return majorRepository.findAll();
    }

    public Major updateMajorByCodeAndDesignation(String code, String designation, Major updated) {
        Major existing = majorRepository.findByCodeAndDesignation(code, designation)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with identifier " + code + "_" + designation));

        existing.setName(updated.getName());
        existing.setTotalCreditsRequired(updated.getTotalCreditsRequired());
        existing.setDescription(updated.getDescription());
        return majorRepository.save(existing);
    }

    public Major updateMajorById(Long id, Major updated) {
        Major existing = majorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + id));

        existing.setName(updated.getName());
        existing.setTotalCreditsRequired(updated.getTotalCreditsRequired());
        existing.setDescription(updated.getDescription());
        existing.setCode(updated.getCode());
        existing.setDesignation(updated.getDesignation());

        return majorRepository.save(existing);
    }

    public void deleteMajorByCodeAndDesignation(String code, String designation) {
        Major major = majorRepository.findByCodeAndDesignation(code, designation)
                .orElseThrow(() -> new ResourceNotFoundException("Major not found with identifier " + code + "_" + designation));
        majorRepository.delete(major);
    }






}
