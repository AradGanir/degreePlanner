package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.MajorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
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

        assertThat(majorRepository.existsById(savedMajor.getId())).isTrue();
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
        Major result = majorService.getById(saved.getId());

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
        assertThatThrownBy(() -> majorService.getById(nonExistentId)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Major not found with id " + nonExistentId);
    }

    @Test
    void createMajor_duplicateCode_throwsException() {
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

        Major major3 = new Major(
                "Mathematics",
                "1111",
                "BS",
                "Math major",
                126
        );
        Major saved = majorService.createMajor(major);
        assertThatThrownBy(() -> majorService.createMajor(major2)).isInstanceOf(DuplicateResourceException.class).hasMessage("Major with code " + "MATH" + " already exists");
        assertThatThrownBy(() -> majorService.createMajor(major3)).isInstanceOf(DuplicateResourceException.class).hasMessage("Major with name " + "Mathematics" + " already exists");


    }
}
