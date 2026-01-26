package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.MajorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class MajorServiceTest {
    @Autowired
    private MajorService majorService;

    @Autowired
    private MajorRepository majorRepository; // Dependency injection framework


    @Test
    void create_Major_validData_savesMajor() {
        Major major = new Major(
                "Computer science",
                "CS",
                "BS",
                "Compsci major",
                126
        );

        Major savedMajor = majorService.createMajor(major);

        assertThat(savedMajor).isNotNull();
        assertThat(savedMajor.getName()).isEqualTo("Computer science");
        assertThat(savedMajor.getId()).isNotNull();

        assertThat(majorService.majorExistsById(savedMajor.getId()));
    }

    @Test
    void getMajor_exists_returnsMajor() {
        Major major = new Major(
                "Mathematics",
                "MATH",
                "BS",
                "Math major",
                126
        );

        Major saved = majorRepository.save(major);
        Major result = majorService.getMajorById(saved.getId());

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo((saved.getId()));
        assertThat(result.getName()).isEqualTo("Mathematics");
        assertThat(result.getCode()).isEqualTo("MATH");
        assertThat(result.getDescription()).isEqualTo("Math major");
        assertThat(result.getDesignation()).isEqualTo("BS");

    }

    @Test
    void getMajor_notFound_throwsResourceNotFoundException() {
        Long nonExistentId = 123L;
        assertThatThrownBy(() -> majorService.getMajorById(nonExistentId)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Major not found with id " + nonExistentId);
    }

    @Test
    void createMajor_duplicateCodeDesignation_throwsException() {
        Major major = new Major(
                "Mathematics",
                "MATH",
                "BS",
                "Math major",
                126
        );

        Major major2 = new Major(
                "Meth",
                "MATH",
                "BS",
                "Math major",
                126
        );

        majorService.createMajor(major);
        assertThatThrownBy(() -> majorService.createMajor(major2))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Major with code MATH and designation BS already exists");
    }

    @Test
    void getAllMajors_majorsExist_returnsAllMajors() {
        Major major = new Major(
                "Mathematics",
                "MATH",
                "BS",
                "Math major",
                126
        );

        Major major2 = new Major(
                "Computer science",
                "CS",
                "BS",
                "Compsci major",
                126
        );


        Major saved1 = majorRepository.save(major);
        Major saved2 = majorRepository.save(major2);

        List<Major> results = majorService.getAllMajors();

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).extracting(Major::getCode).containsExactlyInAnyOrder("MATH", "CS");

    }

    @Test void getAllMajors_noMajors_returnsEmptyList() {
        List<Major> results = majorService.getAllMajors();
        assertThat(results).isEmpty();
        assertThat(results).isNotNull();
    }

    @Test
    void updateMajor_validData_updatesAndReturnsMajor() {
        Major major = new Major(
                "Mathematics",
                "MATH",
                "BS",
                "Math major",
                126
        );

        Major saved = majorRepository.save(major);

        Long major_id = saved.getId();
        assertThat(major.getId()).isEqualTo(major_id);
        assertThat(major.getName()).isEqualTo("Mathematics");
        assertThat(major.getCode()).isEqualTo("MATH");
        assertThat(major.getDescription()).isEqualTo("Math major");
        assertThat(major.getDesignation()).isEqualTo("BS");
        assertThat(major.getTotalCreditsRequired()).isEqualTo(126);

        Major updatedData = new Major (
                "Applied mathematics",
                "MATH",
                "BS",
                "Applied math major",
                120
        );
        Major updated = majorService.updateMajorByCodeAndDesignation(saved.getCode(), saved.getDesignation(), updatedData);

        assertThat(updated).isNotNull();
        assertThat(updated.getName()).isEqualTo("Applied mathematics");
        assertThat(updated.getCode()).isEqualTo("MATH");
        assertThat(updated.getDescription()).isEqualTo("Applied math major");
        assertThat(updated.getDesignation()).isEqualTo("BS");
        assertThat(updated.getTotalCreditsRequired()).isEqualTo(120);
        assertThat(updated.getId()).isEqualTo(major_id);
    }

    @Test
    void updateMajor_notFound_throwsException() {
        Major major = new Major(
                "Mathematics",
                "MATH",
                "BS",
                "Math major",
                126
        );

        Major saved = majorRepository.save(major);

        Long major_id = saved.getId();
        assertThat(major.getId()).isEqualTo(major_id);
        assertThat(major.getName()).isEqualTo("Mathematics");
        assertThat(major.getCode()).isEqualTo("MATH");
        assertThat(major.getDescription()).isEqualTo("Math major");
        assertThat(major.getDesignation()).isEqualTo("BS");
        assertThat(major.getTotalCreditsRequired()).isEqualTo(126);

        Major updatedData = new Major (
                "Applied mathematics",
                "MATH",
                "BS",
                "Applied math major",
                120
        );

        assertThatThrownBy(() -> majorService.updateMajorByCodeAndDesignation("CS", "BS", updatedData)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Major not found with identifier CS_BS");
    }

    @Test
    void deleteMajor_exists_deletesMajor() {
        Major major = new Major(
                "Mathematics",
                "MATH",
                "BS",
                "Math major",
                126
        );

        majorService.createMajor(major);

        assertThat(major).isNotNull();
        assertThat(majorService.majorExistsById(major.getId()));
        majorService.deleteMajorByCodeAndDesignation("MATH", "BS");
        assertThat(majorService.majorExistsById(major.getId())).isFalse();
    }

    @Test
    void deleteMajor_notFound_throwsException() {
        assertThatThrownBy(() -> majorService.deleteMajorByCodeAndDesignation("CS", "BS")).isInstanceOf(ResourceNotFoundException.class).hasMessage("Major not found with identifier CS_BS");
    }


}
