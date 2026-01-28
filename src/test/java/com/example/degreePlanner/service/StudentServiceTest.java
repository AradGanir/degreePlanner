package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class StudentServiceTest {
    @Autowired
    private StudentService studentService;

    Student arad;
    Student arad2;

    @BeforeEach
    void setUp() {
        arad = new Student("AG123", "Arad", "Ganir", "arad@gmail.com");
        arad2 = new Student("AG456", "Arad2", "Ganir2", "arad2@gmail.com");
    }

    @Test
    void createStudent_validData_savesStudent() {
        Student student = studentService.createStudent(arad);
        assertThat(student.getId()).isNotNull();
        assertThat(student.getStudentId()).isEqualTo("AG123");
        assertThat(student.getFirstName()).isEqualTo("Arad");
    }

    @Test
    void createStudent_duplicateStudentId_throwsException() {
        arad2.setStudentId(arad.getStudentId());
        studentService.createStudent(arad);
        assertThatThrownBy(() -> studentService.createStudent(arad2)).isInstanceOf(DuplicateResourceException.class).hasMessage("Student with id" + arad.getStudentId() + " already exists");
    }

    @Test
    void createStudent_duplicateEmail_throwsException() {
        arad2.setEmail(arad.getEmail());
        studentService.createStudent(arad);
        assertThatThrownBy(() -> studentService.createStudent(arad2)).isInstanceOf(DuplicateResourceException.class).hasMessage("Student with email" + arad.getEmail() + " already exists");
    }

    @Test
    void getStudent_exists_returnsStudent() {
        studentService.createStudent(arad);

        Long id = arad.getId();

        studentService.getStudentById(id);

        assertThat(studentService.getStudentById(id)).isEqualTo(arad);
        assertThat(studentService.getStudentById(id)).isNotEqualTo(arad2);
        assertThat(studentService.getStudentById(id).getFirstName()).isEqualTo("Arad");
    }

    @Test
    void getStudent_notExists_throwsException() {
        assertThatThrownBy(()->studentService.getStudentById(1000L)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Student not found with id 1000");
    }

    @Test
    void getAlStudents_returnsAllStudents() {
        studentService.createStudent(arad);
        studentService.createStudent(arad2);

        List<Student> students = studentService.getAllStudents();

        assertThat(students.size()).isEqualTo(2);
        assertThat(students.get(0).getFirstName()).isEqualTo("Arad");
        assertThat(students.get(1).getLastName()).isEqualTo("Ganir2");
    }

    @Test
    void getAllStudents_empty_returnsEmptyList() {
        List<Student> students = studentService.getAllStudents();
        assertThat(students.size()).isEqualTo(0);
    }

    @Test
    void updateStudent_validData_updatesStudent() {
        Student Arad = studentService.createStudent(arad);

        Long id = arad.getId();

        assertThat(studentService.getStudentById(arad.getId())).isEqualTo(arad);
        assertThat(Arad.getFirstName()).isEqualTo("Arad");
        assertThat(Arad.getLastName()).isEqualTo("Ganir");

        arad.setFirstName("NotArad");
        arad.setLastName("notGanir");

        Student arad_new = studentService.updateStudent(id, arad);;
        assertThat(arad_new.getFirstName()).isEqualTo("NotArad");
        assertThat(arad_new.getLastName()).isEqualTo("notGanir");
    }

    @Test
    void updateStudent_duplicateEmail_throwsException() {
        studentService.createStudent(arad);
        Student savedArad2 = studentService.createStudent(arad2);

        // Create a new unmanaged Student object to avoid Hibernate dirty checking flushing prematurely
        Student updateData = new Student(savedArad2.getStudentId(), savedArad2.getFirstName(), savedArad2.getLastName(), arad.getEmail());

        assertThatThrownBy(()->studentService.updateStudent(savedArad2.getId(), updateData)).isInstanceOf(DuplicateResourceException.class).hasMessage("Student with email" + arad.getEmail() + " already exists");

    }

    @Test
    void updateStudent_notFound_throwsException() {
        studentService.createStudent(arad);
        assertThatThrownBy(()->studentService.updateStudent(1000L, arad2)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Student with id 1000 not found");
    }

    @Test
    void deleteStudent_validData_deletesStudent() {
        studentService.createStudent(arad);
        Long id = arad.getId();

        studentService.deleteStudent(id);
        assertThatThrownBy(()-> studentService.getStudentById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Student not found with id " + id);
    }

}
