package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.Prerequisite;
import com.example.degreePlanner.entity.PrerequisiteItem;
import com.example.degreePlanner.entity.PrerequisiteType;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.PrerequisiteItemRepository;
import com.example.degreePlanner.repository.PrerequisiteRepository;
import com.example.degreePlanner.service.CourseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import com.example.degreePlanner.entity.Course;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CourseControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseService courseService;

    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private PrerequisiteRepository prerequisiteRepository;
    @Autowired
    private PrerequisiteItemRepository prerequisiteItemRepository;

    @BeforeEach
    void setup() {
        courseRepository.deleteAll();
        prerequisiteRepository.deleteAll();
        prerequisiteItemRepository.deleteAll();

    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Course math250() {
        return new Course(
                "MATH",
                "250",
                "Introduction to Mathematics",
                "Starting off the math world",
                3
        );
    }

    private Course math111() {
        return new Course(
                "MATH",
                "111",
                "Calculus 1",
                "Diferentiation and Integration",
                3
        );
    }

    private Course cs170() {
        return new Course(
                "CS",
                "170",
                "Intro to CS",
                "Introduction to compsci",
                4
        );
    }


    @Test
    void createCourse_validRequest_returns201() throws Exception {
        Course course = math250();

        mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(course)))
                .andExpect(status().isCreated());
    }

    @Test
    void createCourse_duplicateCodeAndCourseNum_returns409() throws Exception {
        Course course1 =  math250();
        Course course2 = math250();

        mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(course1)))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(course2)))
                .andExpect(status().isConflict());
    }

    @Test
    void getAllCourses_returns200() throws Exception{
        Course course2 =  courseService.createCourse(math111());
        Course course =  courseService.createCourse(math250());
        Course course3 =  courseService.createCourse(cs170());

        mockMvc.perform(get("/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].code").value("MATH"));
    }

    @Test
    void getCourseByCodeAndCourseNum_exists_returns200() throws Exception {
        Course course2 =  courseService.createCourse(math111());

        mockMvc.perform(get("/courses/{code}/{courseNum}", "MATH", 111))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("MATH"))
                .andExpect(jsonPath("$.courseNum").value(111));
    }
    @Test
    void getCourseByCodeAndCourseNum_notFound_returns404() throws Exception{
        mockMvc.perform(get("/courses/{code}/{courseNum}", "MATH", 111))
                .andExpect(status().isNotFound());
    }
    @Test
    void updateCourseByCodeAndCourseNum_validRequest_returns200() throws Exception{
        Course course = courseService.createCourse(math111());

        course.setTitle("Title 2");
        mockMvc.perform(put("/courses/{code}/{courseNum}", "MATH", 111)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(course)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title 2"));
    }
    @Test
    void updateCourseByCodeAndCourseNum_notFound_returns404() throws Exception{
        Course course = math111();
        mockMvc.perform(put("/courses/{code}/{courseNum}", "MATH", 111)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(course)))
                .andExpect(status().isNotFound());
    }
    @Test
    void deleteCourseByCodeAndCourseNum_exists_returns204() throws Exception{
        courseService.createCourse(math111());
        mockMvc.perform(delete("/courses/{code}/{courseNum}", "MATH", 111))
                .andExpect(status().isNoContent());
    }
    @Test
    void deleteCourseByCodeAndCourseNum_notFound_returns404() throws Exception{
        mockMvc.perform(delete("/courses/{code}/{courseNum}", "MATH", 111))
                .andExpect(status().isNotFound());
    }

    // Prerequisites Test
    // TODO Create all my prerequisite tests, once i can figure out how to bypass the jackson issues

    @Test
    void setPrerequisite_validRequest_returns200() throws Exception {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        String requestBody = String.format("""
            {
                "type": "AND",
                "courseIds": [%d, %d]
            }
            """, math111.getId(), math112.getId());

        mockMvc.perform(put("/courses/{code}/{courseNum}/prerequisite", "MATH", "250")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("AND"))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void setPrerequisite_courseNotFound_returns404() throws Exception {
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        String requestBody = String.format("""
            {
                "type": "AND",
                "courseIds": [%d, %d]
            }
            """, math111.getId(), math112.getId());

        mockMvc.perform(put("/courses/{code}/{courseNum}/prerequisite", "MATH", "250")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void setPrerequisite_prerequisiteCourseNotFound_returns404() throws Exception {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));

        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        Long id = 100000L;

        String requestBody = String.format("""
            {
                "type": "AND",
                "courseIds": [%d, %d]
            }
            """, math111.getId(), id);

        mockMvc.perform(put("/courses/{code}/{courseNum}/prerequisite", "MATH", "250")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPrerequisites_exists_returns200() throws Exception {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.AND,
                List.of(math111.getId(), math112.getId())
        );

        mockMvc.perform(get("/courses/{code}/{courseNum}/prerequisite", "MATH", "250"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("AND"))
                .andExpect(jsonPath("$.items.length()").value(2));
    }

    @Test
    void getPrerequisites_notExists_ReturnsEmpty() throws Exception {
        Course math210 = courseService.createCourse(new Course("MATH", "210", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        mockMvc.perform(get("/courses/{code}/{courseNum}/prerequisite", "MATH", "210"))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

}
