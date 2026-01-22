package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.MajorRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
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

    public Major getById(Long id) {
        return majorRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Major not found with id " + id));
    }


}
