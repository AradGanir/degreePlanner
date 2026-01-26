package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Requirement;
import com.example.degreePlanner.entity.RequirementType;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.MajorRepository;
import com.example.degreePlanner.repository.RequirementRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class RequirementControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private RequirementRepository requirementRepository;



    private Major mathMajor;
    private Major csMajor;
    private Course math111;
    private Course math112;
    private Course cs170;

    @BeforeEach
    void setUp() {
        requirementRepository.deleteAll();
        courseRepository.deleteAll();
        majorRepository.deleteAll();

        mathMajor = majorRepository.save(new Major("Mathematics", "MATH", "BS", "Math major", 120));
        csMajor = majorRepository.save(new Major("Computer Science", "CS", "BS", "CS major", 120));

        // Save courses to DB
        math111 = courseRepository.save(new Course("MATH", 111, "Calculus I", "Intro calc", 4));
        math112 = courseRepository.save(new Course("MATH", 112, "Calculus II", "More calc", 4));
        cs170 = courseRepository.save(new Course("CS", 170, "Intro to CS", "Intro course", 4));
    }
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Test
    void createRequirement_validRequest_returns201() throws Exception {
        Requirement requirement = new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee");
        mockMvc.perform(post("/majors/{code}/{designation}/requirements", mathMajor.getCode(), mathMajor.getDesignation())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requirement))) // serialize Major to JSON
                .andExpect(status().isCreated());
    }

    @Test
    void createRequirement_majorNotFound_returns404() throws Exception {
        Requirement requirement = new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee");
        Major math = new Major("Mathematics", "M", "B", "Math major", 120);
        mockMvc.perform(post("/majors/{code}/{designation}/requirements", math.getCode(), math.getDesignation())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requirement))) // serialize Major to JSON
                .andExpect(status().isNotFound());
    }

    @Test
    void createRequirement_invalidType_returns400() throws Exception {
        String invalidJson = """
                {
                    "type": "INVALID_TYPE",
                    "name": "INVALID NAME",
                    "min_credits": 8
                }
                """;
        mockMvc.perform(post("/majors/{code}/{designation}/requirements", mathMajor.getCode(), mathMajor.getDesignation())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(invalidJson))) // serialize Major to JSON
                .andExpect(status().isBadRequest());
    }

    @Test
    void getRequirement_exists_returns200() throws Exception {
        Requirement requirement = new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee");
        requirementRepository.save(requirement);

        mockMvc.perform(get("/requirements/{id}", requirement.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void getRequirement_notFound_returns404() throws Exception {
        mockMvc.perform(get("/requirements/{id}", 1))
                .andExpect(status().isNotFound());
    }

    @Test
    void getRequirementsByMajor_returns200() throws Exception {
        Requirement requirement = new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee");
        Requirement requirement2 = new Requirement(mathMajor, RequirementType.CORE, "Real Analysis", 8, "Core analysis coursee");
        requirementRepository.save(requirement);
        requirementRepository.save(requirement2);

        mockMvc.perform(get("/majors/{code}/{designation}/requirements", mathMajor.getCode(), mathMajor.getDesignation()))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(status().isOk());
    }

    @Test
    void getRequirementsByMajor_noRequirements_returnsEmptyList() throws Exception {
        mockMvc.perform(get("/majors/{code}/{designation}/requirements", mathMajor.getCode(), mathMajor.getDesignation()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRequirementsByMajor_majorNotFound_returns404() throws Exception {
        mockMvc.perform(get("/majors/{code}/{designation}/requirements", "ART", mathMajor.getDesignation()))
                .andExpect(status().isNotFound());
    }


    @Test
    void updateRequirement_validRequest_returns200() throws Exception {
        Requirement requirement = new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee");
        Requirement requirement2 = new Requirement(mathMajor, RequirementType.CORE, "Real Analysis", 8, "Core analysis courses");

        requirementRepository.save(requirement);

        mockMvc.perform(put("/requirements/{id}", requirement.getId(), requirement2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requirement2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Core analysis courses"));
    }

    @Test
    void updateRequirement_notFound_returns404() throws Exception {
        Requirement requirement = new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee");

        mockMvc.perform(put("/requirements/{id}", 1000L, requirement)
                        .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requirement)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRequirement_validRequest_returns204() throws Exception {
        Requirement req = requirementRepository.save(new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee"));
        Long id = req.getId();
        mockMvc.perform(delete("/requirements/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteRequirement_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/requirements/{id}", 1000L))
                .andExpect(status().isNotFound());
    }

    @Test
    void addCourseToRequirement_validRequest_returns200() throws Exception {
        Requirement req = requirementRepository.save(new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee"));
        Long math111id = math111.getId();
        mockMvc.perform(post("/requirements/{id}/courses/{courseId}", req.getId(), math111id))
                .andExpect(status().isCreated());
    }

    @Test
    void addCourseToRequirement_courseNotFound_returns404() throws Exception {
        Requirement req = requirementRepository.save(new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee"));
        Long id = 1000L;
        mockMvc.perform(post("/requirements/{id}/courses/{courseId}", req.getId(), id))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeCourseFromRequirement_validRequest_returns204() throws Exception {
        Long math111id = math111.getId();
        Requirement req = requirementRepository.save(new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee"));
        mockMvc.perform(delete("/requirements/{id}/courses/{courseId}", req.getId(), math111id))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeCourseFromRequirement_missingCourse_returns404() throws Exception {
        Long math111id = 1000L;
        Requirement req = requirementRepository.save(new Requirement(mathMajor, RequirementType.CORE, "Calc sequence", 8, "Core calc coursee"));
        mockMvc.perform(delete("/requirements/{id}/courses/{courseId}", req.getId(), math111id))
                .andExpect(status().isNotFound());
    }





}
