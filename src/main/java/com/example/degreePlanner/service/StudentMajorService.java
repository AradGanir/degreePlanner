package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.entity.StudentMajor;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.MajorRepository;
import com.example.degreePlanner.repository.StudentMajorRepository;
import com.example.degreePlanner.repository.StudentRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class StudentMajorService {

    private StudentRepository studentRepository;
    private MajorRepository majorRepository;
    private StudentMajorRepository studentMajorRepository;

    StudentMajorService(StudentRepository studentRepository, MajorRepository majorRepository, StudentMajorRepository studentMajorRepository) {
        this.studentRepository = studentRepository;
        this.majorRepository = majorRepository;
        this.studentMajorRepository = studentMajorRepository;
    }

    public StudentMajor declareMajor(Long id, String majorCode, String majorDesignation, boolean isPrimary){
        Student student = studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
        Major major = majorRepository.findByCodeAndDesignation(majorCode, majorDesignation).orElseThrow(() -> new ResourceNotFoundException("Major not found " + majorCode + majorDesignation));

        if (studentMajorRepository.existsByStudentAndMajor(student, major)) {
            throw new DuplicateResourceException("Student " + student.getFirstName() + " " + student.getLastName() + " with " + majorCode + majorDesignation + " already exists");
        }

        long currentCount = studentMajorRepository.countByStudent(student);
        if (currentCount >= 2) {
            throw new IllegalStateException("Student " + student.getFirstName() + " " + student.getLastName() + " can't have more than 2 majors");
        }

        if (isPrimary) {
            List<StudentMajor> existingMajors =
                    studentMajorRepository.findByStudent(student);
            for (StudentMajor existing : existingMajors) {
                if (Boolean.TRUE.equals(existing.getIsPrimary())) {
                    existing.setIsPrimary(false);
                    studentMajorRepository.save(existing);
                }
            }
        }
        StudentMajor studentMajor = new StudentMajor(student, major,
                LocalDate.now(), isPrimary);
        return studentMajorRepository.save(studentMajor);
    }

    public List<StudentMajor> getStudentMajors(Long id){
        return studentMajorRepository.findAll();
    }

    public void removeMajor(Long id, String majorCode, String majorDesignation){
        Student student = studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
        Major major = majorRepository.findByCodeAndDesignation(majorCode, majorDesignation).orElseThrow(() -> new ResourceNotFoundException("Major not found " + majorCode + majorDesignation));

        StudentMajor studentMajor = studentMajorRepository.findByStudentAndMajor(student, major).orElseThrow(() -> new ResourceNotFoundException("Student does not have this major declared"));
        boolean wasPrimary = Boolean.TRUE.equals(studentMajor.getIsPrimary());
        studentMajorRepository.delete(studentMajor);

        if (wasPrimary) {
            List<StudentMajor> remaining =
                    studentMajorRepository.findByStudent(student);
            if (!remaining.isEmpty()) {
                remaining.get(0).setIsPrimary(true);
                studentMajorRepository.save(remaining.get(0));
            }
        }
    }

    public StudentMajor setPrimaryMajor(String studentId, String majorCode,
                                        String majorDesignation) {
        // 1. Find the student
        Student student = studentRepository.findByStudentId(studentId).orElseThrow(() -> new ResourceNotFoundException("Student not found with studentId: " + studentId));

        // 2. Find the major
        Major major = majorRepository.findByCodeAndDesignation(majorCode, majorDesignation).orElseThrow(() -> new ResourceNotFoundException("Major not found: " + majorCode + " " + majorDesignation));

        // 3. Find the StudentMajor link
        StudentMajor studentMajor = studentMajorRepository.findByStudentAndMajor(student, major).orElseThrow(() -> new ResourceNotFoundException("Student does not have this major declared"));

        // 4. Already primary? Just return
        if (Boolean.TRUE.equals(studentMajor.getIsPrimary())) {
            return studentMajor;
        }

        // 5. Unset any existing primary
        studentMajorRepository.clearPrimaryForStudent(student);

        // 6. Set this one as primary
        studentMajor.setIsPrimary(true);
        return studentMajorRepository.save(studentMajor);
    }

    public Long getMajorCount(Long id){
        Student student = studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
        long currentCount = studentMajorRepository.countByStudent(student);
        return currentCount;
    }

}
