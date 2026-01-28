package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.StudentRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;


    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public Student createStudent(Student student) {
        if(studentRepository.existsByStudentId(student.getStudentId())) {
            throw new DuplicateResourceException("Student with id" + student.getStudentId() + " already exists");
        }
        if(studentRepository.existsByEmail(student.getEmail())) {
            throw new DuplicateResourceException("Student with email" + student.getEmail() + " already exists");
        }
        return studentRepository.save(student);
    }

    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student not found with id " + id));
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Student updateStudent(Long id, Student student) {
        Student old_student = studentRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("Student with id " + id + " not found"));
        if(studentRepository.existsByStudentIdAndIdNot(student.getStudentId(), id)) {
            throw new DuplicateResourceException("Student with id" + student.getStudentId() + " already exists");
        }
        if(studentRepository.existsByEmailAndIdNot(student.getEmail(), id)) {
            throw new DuplicateResourceException("Student with email" + student.getEmail() + " already exists");
        }
        old_student.setEmail(student.getEmail());
        old_student.setFirstName(student.getFirstName());
        old_student.setLastName(student.getLastName());
        old_student.setStudentId(student.getStudentId());
        return studentRepository.save(old_student);
    }

    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student with id " + id + " not found"));
        studentRepository.delete(student);
    }



}
