package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.entity.StudentMajor;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.StudentMajorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class StudentMajorServiceTest {

    @Autowired
    private StudentMajorService studentMajorService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private MajorService majorService;

    Student student;
    Major major;
    Major major2;
    @Autowired
    private StudentMajorRepository studentMajorRepository;

    @BeforeEach
    public void setup() {
        student = new Student("AGANIR", "Arad", "Ganir", "arad@ganir.net");
        major = new Major("Applied Mathematics and Statistics", "AMS", "BS", "QTM but MAth", 56);
        major2 = new Major("Computer Science", "CS", "BA", "CS", 50);

        studentMajorRepository.deleteAll();

        studentService.createStudent(student);
        majorService.createMajor(major);
        majorService.createMajor(major2);
    }

    @Test
    void declareMajor_validData_declaresMajor() {
        StudentMajor declared_major = studentMajorService.declareMajor(student.getId(), "AMS", "BS", false);



        assertThat(declared_major).isNotNull();
        assertThat(declared_major.getIsPrimary()).isFalse();
        assertThat(studentMajorService.getStudentMajors(student.getId()).size()).isEqualTo(1);

    }

    @Test
    void declareMajor_studentNotFound_throwsException() {
        Student st = new Student("AAAAA", "d", "a", "a");

        Long id = st.getId();
        assertThatThrownBy(() -> studentMajorService.declareMajor(1000L, "AMS", "BS", false)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Student not found with id " +  1000L);
    }

    @Test
    void declareMajor_majorNotFound_throwsException() {
        assertThatThrownBy(() -> studentMajorService.declareMajor(student.getId(), "AMS", "BA", false)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Major not found AMSBA");
    }

    @Test
    void declareMajor_majorAlreadyDeclared_throwsException() {
        studentMajorService.declareMajor(student.getId(), "AMS", "BS", false);
        assertThatThrownBy(() -> studentMajorService.declareMajor(student.getId(), "AMS", "BS", false)).isInstanceOf(DuplicateResourceException.class).hasMessage("Student Arad Ganir with AMSBS already exists");
    }

    @Test
    void declareMajor_maxMajorsReached_throwsException() {
        Major major3 = majorService.createMajor(new Major("Fake", "FK","BS","tesT", 100));

        studentMajorService.declareMajor(student.getId(), "AMS", "BS", false);
        studentMajorService.declareMajor(student.getId(), "CS", "BA", false);
        assertThatThrownBy(() -> studentMajorService.declareMajor(student.getId(), "FK", "BS", false)).isInstanceOf(IllegalStateException.class).hasMessage("Student Arad Ganir can't have more than 2 majors");
    }

    @Test
    void declareMajor_setPrimary_updatesPrimary() {
        //studentMajorService.declareMajor(student.getId(), "AMS", "BS", true);
        StudentMajor first = studentMajorService.declareMajor(student.getId(), major.getCode(), major.getDesignation(), true);
        assertThat(first.getIsPrimary()).isTrue();

        StudentMajor second = studentMajorService.declareMajor(student.getId(), major2.getCode(), major2.getDesignation(), true);
        assertThat(second.getIsPrimary()).isTrue();
        assertThat(first.getIsPrimary()).isFalse();
    }

    @Test
    void getStudentMajors_returnsDeclaredMajors() {
        StudentMajor first = studentMajorService.declareMajor(student.getId(), major.getCode(), major.getDesignation(), true);
        assertThat(first.getIsPrimary()).isTrue();

        StudentMajor second = studentMajorService.declareMajor(student.getId(), major2.getCode(), major2.getDesignation(), true);
        assertThat(second.getIsPrimary()).isTrue();

        List<StudentMajor> studentMajors =  studentMajorService.getStudentMajors(student.getId());

        assertThat(studentMajors.size()).isEqualTo(2);
        assertThat(studentMajors.get(0).getMajor().getCode()).isEqualTo(first.getMajor().getCode());
        assertThat(studentMajors.get(1).getMajor().getDesignation()).isEqualTo(second.getMajor().getDesignation());
    }

    @Test
    void getStudentMajors_noMajors_returnsEmptyList() {
        List<StudentMajor> studentMajors = studentMajorService.getStudentMajors(student.getId());
        assertThat(studentMajors.size()).isEqualTo(0);
        assertThat(studentMajors).isNotNull();
    }

    @Test
    void removeMajor_exists_removesMajor() {
        studentMajorService.declareMajor(student.getId(), "AMS", "BS", false);
        assertThat(studentMajorService.getStudentMajors(student.getId()).size()).isEqualTo(1);
        studentMajorService.declareMajor(student.getId(), "CS", "BA", false);
        assertThat(studentMajorService.getStudentMajors(student.getId()).size()).isEqualTo(2);
        studentMajorService.removeMajor(student.getId(), "CS", "BA");
        assertThat(studentMajorService.getStudentMajors(student.getId()).size()).isEqualTo(1);
        studentMajorService.removeMajor(student.getId(), "AMS", "BS");
        assertThat(studentMajorService.getStudentMajors(student.getId()).size()).isEqualTo(0);
    }

    @Test
    void removeMajor_notDeclared_throwsException() {
        assertThatThrownBy(()-> studentMajorService.removeMajor(student.getId(), "AMS", "BS")).isInstanceOf(ResourceNotFoundException.class).hasMessage("Student does not have this major declared");
    }

}
