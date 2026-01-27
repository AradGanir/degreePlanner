package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Major;
import com.example.degreePlanner.entity.Requirement;
import com.example.degreePlanner.entity.RequirementType;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.CourseRepository;
import com.example.degreePlanner.repository.MajorRepository;
import com.example.degreePlanner.repository.RequirementRepository;
import org.junit.jupiter.api.BeforeEach;
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
public class RequirementServiceTest {

    @Autowired
    private RequirementService requirementService;

    @Autowired
    private RequirementRepository requirementRepository;

    @Autowired
    private MajorRepository majorRepository;

    @Autowired
    private CourseRepository courseRepository;

    // Test data - set in @BeforeEach
    private Major mathMajor;
    private Major csMajor;
    private Course math111;
    private Course math112;
    private Course cs170;

    @BeforeEach
    void setup() {
        // Save majors to DB
        mathMajor = majorRepository.save(new Major("Mathematics", "MATH", "BS", "Math major", 120));
        csMajor = majorRepository.save(new Major("Computer Science", "CS", "BS", "CS major", 120));

        // Save courses to DB
        math111 = courseRepository.save(new Course("MATH", "111", "Calculus I", "Intro calc", 4));
        math112 = courseRepository.save(new Course("MATH", "112", "Calculus II", "More calc", 4));
        cs170 = courseRepository.save(new Course("CS", "170", "Intro to CS", "Intro course", 4));
    }

    // ========== CREATE ==========

    @Test
    void createRequirement_validData_savesRequirement() {
        Requirement req = new Requirement(mathMajor, RequirementType.CORE, "Calculus Sequence", 8, "Core calc courses");

        Requirement saved = requirementService.createRequirement("MATH", "BS", req);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Calculus Sequence");
        assertThat(saved.getType()).isEqualTo(RequirementType.CORE);
        assertThat(saved.getMinCredits()).isEqualTo(8);
    }

    @Test
    void createRequirement_majorNotFound_throwsException() {
        Requirement req = new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc");

        assertThatThrownBy(() -> requirementService.createRequirement("FAKE", "BS", req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== GET ==========

    @Test
    void getRequirementsByMajor_exists_returnsRequirements() {
        requirementRepository.save(new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));
        requirementRepository.save(new Requirement(mathMajor, RequirementType.ELECTIVE, "Electives", 20, "desc"));

        List<Requirement> results = requirementService.getRequirementsByMajor("MATH", "BS");

        assertThat(results).hasSize(2);
    }

    @Test
    void getRequirementsByMajor_empty_returnsEmptyList() {
        List<Requirement> results = requirementService.getRequirementsByMajor("MATH", "BS");

        assertThat(results).isEmpty();
    }

    @Test
    void getRequirementById_exists_returnsRequirement() {
        Requirement saved = requirementRepository.save(
                new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));

        Requirement result = requirementService.getRequirementById(saved.getId());

        assertThat(result.getName()).isEqualTo("Core");
    }

    @Test
    void getRequirementById_notFound_throwsException() {
        assertThatThrownBy(() -> requirementService.getRequirementById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== UPDATE ==========

    @Test
    void updateRequirement_validData_updatesRequirement() {
        Requirement saved = requirementRepository.save(
                new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));

        Requirement update = new Requirement(mathMajor, RequirementType.CORE, "Updated Name", 15, "new desc");
        Requirement result = requirementService.updateRequirement(saved.getId(), update);

        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getMinCredits()).isEqualTo(15);
    }

    @Test
    void updateRequirement_notFound_throwsException() {
        Requirement update = new Requirement(mathMajor, RequirementType.CORE, "Name", null, "desc");

        assertThatThrownBy(() -> requirementService.updateRequirement(999L, update))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== DELETE ==========

    @Test
    void deleteRequirement_exists_deletesRequirement() {
        Requirement saved = requirementRepository.save(
                new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));

        requirementService.deleteRequirement(saved.getId());

        assertThat(requirementRepository.findById(saved.getId())).isEmpty();
    }

    @Test
    void deleteRequirement_notFound_throwsException() {
        assertThatThrownBy(() -> requirementService.deleteRequirement(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ========== COURSE MANAGEMENT ==========

    @Test
    void addCourseToRequirement_validData_addsCourse() {
        Requirement saved = requirementRepository.save(
                new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));

        requirementService.addCourseToRequirement(saved.getId(), math111.getId());

        Requirement result = requirementRepository.findById(saved.getId()).get();
        assertThat(result.getCourses()).contains(math111);
    }

    @Test
    void removeCourseFromRequirement_exists_removesCourse() {
        Requirement req = new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc");
        req.getCourses().add(math111);
        Requirement saved = requirementRepository.save(req);

        requirementService.removeCourseFromRequirement(saved.getId(), math111.getId());

        Requirement result = requirementRepository.findById(saved.getId()).get();
        assertThat(result.getCourses()).doesNotContain(math111);
    }

    @Test
    void addCourseToRequirement_requirementNotFound_throwsException() {
        assertThatThrownBy(() -> requirementService.addCourseToRequirement(999L, math111.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Requirement not found");
    }

    @Test
    void addCourseToRequirement_courseNotFound_throwsException() {
        Requirement saved = requirementRepository.save(
                new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));

        assertThatThrownBy(() -> requirementService.addCourseToRequirement(saved.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

    @Test
    void removeCourseFromRequirement_requirementNotFound_throwsException() {
        assertThatThrownBy(() -> requirementService.removeCourseFromRequirement(999L, math111.getId()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Requirement not found");
    }

    @Test
    void removeCourseFromRequirement_courseNotFound_throwsException() {
        Requirement saved = requirementRepository.save(
                new Requirement(mathMajor, RequirementType.CORE, "Core", null, "desc"));

        assertThatThrownBy(() -> requirementService.removeCourseFromRequirement(saved.getId(), 999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Course not found");
    }

}
