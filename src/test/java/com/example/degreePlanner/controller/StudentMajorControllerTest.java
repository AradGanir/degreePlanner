package com.example.degreePlanner.controller;

import com.example.degreePlanner.dto.request.Student.DeclareMajorRequest;
import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.repository.StudentMajorRepository;
import com.example.degreePlanner.service.MajorService;
import com.example.degreePlanner.service.StudentMajorService;
import com.example.degreePlanner.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class StudentMajorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentMajorService studentMajorService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private StudentMajorRepository studentMajorRepository;

    @Autowired
    private MajorService majorService;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    Student student;
    Major major;
    Major major2;
    Major major3;

    @BeforeEach
    public void setup() {
        student = new Student("AGANIR", "Arad", "Ganir", "arad@ganir.net");
        major = new Major("Applied Mathematics and Statistics", "AMS", "BS", "QTM but MAth", 56);
        major2 = new Major("Computer Science", "CS", "BA", "CS", 50);
        major3 = new Major("Computer Science", "CS", "BS", "CS", 50);

        studentMajorRepository.deleteAll();

        studentService.createStudent(student);
        majorService.createMajor(major);
        majorService.createMajor(major2);
        majorService.createMajor(major3);
    }

    @Test
    void declareMajor_validData_declaresMajor() throws Exception {
        DeclareMajorRequest declareMajorRequest = new DeclareMajorRequest("CS", "BA", true);

        mockMvc.perform(post("/students/{id}/majors", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(declareMajorRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isPrimary").value(true));
    }

    @Test
    void declareMajor_MajorNotFound_returns404() throws Exception {
        DeclareMajorRequest declareMajorRequest = new DeclareMajorRequest("CS", "bba", true);
        mockMvc.perform(post("/students/{id}/majors", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(declareMajorRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void declareMajor_studentNotFound_returns404() throws Exception {
        DeclareMajorRequest declareMajorRequest = new DeclareMajorRequest("CS", "BS", true);
        mockMvc.perform(post("/students/{id}/majors", 1000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(declareMajorRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void declaresMajor_alreadyDeclared_returns409() throws Exception {
        DeclareMajorRequest declareMajorRequest = new DeclareMajorRequest("CS", "BA", true);
        studentMajorService.declareMajor(student.getId(), "CS", "BA", false);
        mockMvc.perform(post("/students/{id}/majors", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(declareMajorRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void declaresMajor_maxMajorsReached_returns409() throws Exception {
        DeclareMajorRequest declareMajorRequest = new DeclareMajorRequest("CS", "BA", true);
        studentMajorService.declareMajor(student.getId(), "AMS", "BS", false);
        studentMajorService.declareMajor(student.getId(), "CS", "BS", false);
        mockMvc.perform(post("/students/{id}/majors", student.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(declareMajorRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void getStudentMajors_returns200() throws Exception {
        studentMajorService.declareMajor(student.getId(), "AMS", "BS", true);
        studentMajorService.declareMajor(student.getId(), "CS", "BS", false);

        mockMvc.perform(get("/students/{id}/majors", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].majorCode").value("AMS"))
                .andExpect(jsonPath("$[1].majorCode").value("CS"));
    }

    @Test
    void removeMajor_exists_returns204() throws Exception {
        studentMajorService.declareMajor(student.getId(), "AMS", "BS", true);

        mockMvc.perform(delete("/students/{studentId}/majors/{majorCode}/{majorDesignation}",
                        student.getId(), "AMS", "BS"))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeMajor_notExists_returns404() throws Exception {
        mockMvc.perform(delete("/students/{studentId}/majors/{majorCode}/{majorDesignation}",
                        student.getId(), "AMS", "BS"))
                .andExpect(status().isNotFound());
    }



}
