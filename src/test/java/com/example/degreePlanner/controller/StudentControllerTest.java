package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.Student;
import com.example.degreePlanner.service.StudentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("Test")
@Transactional
public class StudentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentService studentService;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Student arad;
    private Student arad2;

    @BeforeEach
    public void setup() {
        arad = new Student("AG123", "Arad", "Ganir", "arad@gmail.com");
        arad2 = new Student("AG456", "Arad2", "Ganir2", "arad2@gmail.com");
    }

    @Test
    void createStudent_validRequest_returns201()  throws Exception {
        mockMvc.perform(post("/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(arad)))
                .andExpect(status().isCreated());
    }

    @Test
    void createStudent_duplicateStudentId_returns409()  throws Exception {
        studentService.createStudent(arad);

        arad2.setStudentId(arad.getStudentId());
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(arad2)))
                .andExpect(status().isConflict());
    }

    @Test
    void createStudent_duplicateEmail_returns409()  throws Exception {
        studentService.createStudent(arad);

        arad2.setEmail(arad.getEmail());
        mockMvc.perform(post("/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(arad2)))
                .andExpect(status().isConflict());
    }

    @Test
    void getStudent_exists_returns200() throws Exception {
        Student student = studentService.createStudent(arad);


        mockMvc.perform(get("/students/{studentId}", student.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(student.getStudentId()));
    }

    @Test
    void getStudent_notExists_returns404() throws Exception {
        mockMvc.perform(get("/students/{studentId}", 1000L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllStudents_returns200() throws Exception {
        studentService.createStudent(arad);
        studentService.createStudent(arad2);

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateStudent_validRequest_returns200() throws Exception {
        studentService.createStudent(arad);

        mockMvc.perform(put("/students/{studentId}", arad.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(arad2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(arad2.getStudentId()))
                .andExpect(jsonPath("$.email").value(arad2.getEmail()));
    }

    @Test
    void updateStudent_notFound_returns404() throws Exception {
        mockMvc.perform(put("/students/{studentId}", 1000L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(arad2)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudent_validRequest_returns200() throws Exception {
        studentService.createStudent(arad);
        mockMvc.perform(delete("/students/{studentId}", arad.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void deleteStudent_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/students/{studentId}", 1000L))
                .andExpect(status().isNotFound());
    }




}
