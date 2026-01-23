package com.example.degreePlanner.controller;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.repository.MajorRepository;
import com.example.degreePlanner.service.MajorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import com.fasterxml.jackson.databind.ObjectMapper;



@SpringBootTest
@AutoConfigureMockMvc
public class MajorControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MajorRepository majorRepository;
    @Autowired
    private MajorService majorService;

    @BeforeEach
    void setup() {
        majorRepository.deleteAll();
    }

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //createMajor_validRequest_returns201 DONE
    //createMajor_duplicateCode_returns409 DONE
    //getMajor_exists_returns200 DONE
    //getMajor_notFound_returns404 DONE
    //getAllMajors_returns200 DONE
    //updateMajor_validRequest_returns200 DONE
    //deleteMajor_exists_returns204

    //@PostMapping → 201 Created DONE
    //@GetMapping → 200 OK
    //@GetMapping("/{id}") → 200 OK or 404
    //@PutMapping("/{id}") → 200 OK
    //@DeleteMapping("/{id}") → 204 No Content



    @Test
    void createMajor_validRequest_returns201() throws Exception {
        Major major = new Major("Computer Science", "CS", "BS", "test", 120);

        mockMvc.perform(post("/majors")
            .contentType(MediaType.APPLICATION_JSON)
            .content(asJsonString(major))) // serialize Major to JSON
            .andExpect(status().isCreated());
    }

    @Test
    void createMajor_duplicateCode_returns409() throws Exception {
        Major major1 = new Major("Computer Science", "CS", "BS", "test", 120);
        Major major2 = new Major("Mathematics", "CS","BS", "test", 120);

        mockMvc.perform(post("/majors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(major1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/majors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(major2)))
                .andExpect(status().isConflict());
    }

    @Test
    void getMajor_exists_returns200() throws Exception {
        Major saved = majorRepository.save(new Major("Computer Science", "CS", "BS", "test", 120));

        mockMvc.perform(get("/majors/by-code/{code}", "CS"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Computer Science"));

    }

    @Test
    void getMajor_notFound_returns404() throws Exception {
        mockMvc.perform(get("/majors/by-code/{code}", "CS"))
                .andExpect(status().isNotFound());
    }




    @Test
    void getAllMajors_returns200() throws Exception {
        majorRepository.save(new Major("Computer Science", "CS", "BS", "test", 120));
        majorRepository.save(new Major("Mathematics", "MATH","BS", "test", 120));


        mockMvc.perform(get("/majors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }


    @Test
    void updateMajor_ByCode_validRequest_returns200() throws Exception {
        Major major = majorRepository.save(new Major("Computer Science", "CS", "BS", "test", 120));

        major.setName("Name2");
        mockMvc.perform(put("/majors/by-code/{code}", "CS", major)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(major)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Name2"));

    }

    @Test
    void deleteMajor_validRequest_returns204() throws Exception {
        majorRepository.save(new Major("Computer Science", "CS", "BS", "test", 120));
        mockMvc.perform(delete("/majors/by-code/{code}", "CS"))
                .andExpect(status().isNoContent());
    }
}
