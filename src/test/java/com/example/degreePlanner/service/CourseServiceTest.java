package com.example.degreePlanner.service;

import com.example.degreePlanner.entity.Course;
import com.example.degreePlanner.entity.Prerequisite;
import com.example.degreePlanner.entity.PrerequisiteType;
import com.example.degreePlanner.exception.DuplicateResourceException;
import com.example.degreePlanner.exception.ResourceNotFoundException;
import com.example.degreePlanner.repository.PrerequisiteItemRepository;
import com.example.degreePlanner.repository.PrerequisiteRepository;
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
public class CourseServiceTest {
    //
    //getAllCourses_returnsList
    //updateCourse_validData_updatesAndReturnsCourse
    //deleteCourse_exists_deletesCourse

    @Autowired
    private CourseService courseService;

    @Autowired
    private PrerequisiteItemRepository prerequisiteItemRepository;

    @Autowired
    private PrerequisiteRepository prerequisiteRepository;

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
    void createCourse_validData_savesCourse() {
        Course saved = courseService.createCourse(math250());

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCode()).isEqualTo("MATH");
        assertThat(saved.getCourseNum()).isEqualTo("250");
        assertThat(saved.getTitle()).isEqualTo("Introduction to Mathematics");
    }

    @Test
    void createCourse_duplicateCodeAndCourseNum_throwsException() {
        Course saved = courseService.createCourse(math250());
        assertThatThrownBy(() -> courseService.createCourse(math250())).isInstanceOf(DuplicateResourceException.class).hasMessage("Course with identity MATH250 already exists");
    }

    @Test
    void getAllCourses_returnsAllCourses() {
        Course math250 = math250();
        Course math111 = math111();

        Course saved1 = courseService.createCourse(math250);
        Course saved2 = courseService.createCourse(math111);

        List<Course> results = courseService.getAllCourses();

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).extracting(Course::getCode).containsExactlyInAnyOrder("MATH", "MATH");

    }

    @Test
    void getAllCourses_empty_returnsEmptyList() {
        List<Course> results = courseService.getAllCourses();

        assertThat(results).isNotNull();
        assertThat(results).isEmpty();
    }

    @Test
    void getCourseById_exists_returnsCourse() {
        Course saved = courseService.createCourse(math250());
        Course saved2 = courseService.createCourse(math111());

        Long id1 = saved.getId();
        Long id2 = saved2.getId();

        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();

        assertThat(courseService.getCourseById(id1)).isEqualTo(saved);
        assertThat(courseService.getCourseById(id2)).isEqualTo(saved2);
    }

    @Test
    void getCourseById_notFound_throwsException() {
        assertThatThrownBy(() -> courseService.getCourseById(1L)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course not found with id 1");
    }

    @Test
    void getCourseByCode_exists_returnsCourseList() {
        Course math1 =  courseService.createCourse(math250());
        Course math2 = courseService.createCourse(math111());
        Course cs = courseService.createCourse(cs170());

        List<Course> results =  courseService.getCourseByCode("CS");
        List<Course> results2 =  courseService.getCourseByCode("MATH");

        assertThat(results).isNotNull();
        assertThat(results2.size()).isEqualTo(2);
        assertThat(results.size()).isEqualTo(1);
        assertThat(results2).isNotNull();
    }

    @Test
    void getCourseByCode_notFound_throwsException() {
        assertThatThrownBy(() -> courseService.getCourseByCode("TEST")).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course not found with code TEST");

    }

    @Test
    void getCourseByCodeAndCourseNum_exists_returnsCourse() {
        Course math1 =  courseService.createCourse(math250());
        assertThat(courseService.getCourseByCodeAndCourseNum("MATH", "250").getCode()).isEqualTo("MATH");
        assertThat(courseService.getCourseByCodeAndCourseNum("MATH", "250").getId()).isNotNull();
    }

    @Test
    void getCourseByCodeAndCourseNum_notFound_throwsException() {
        assertThatThrownBy(() -> courseService.getCourseByCodeAndCourseNum("TEST", "250")).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course TEST250 not found");
    }

    @Test
    void updateCourseById_validData_updatesAndReturnsCourse() {
        Course course = math250();

        Course saved = courseService.createCourse(course);

        Long course_id = saved.getId();

        Course updatedCourse = math111();

        Course updated = courseService.updateCourseById(course_id, updatedCourse);

        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("Calculus 1");
        assertThat(updated.getCode()).isEqualTo("MATH");
        assertThat(updated.getCourseNum()).isEqualTo("111");

    }

    @Test
    void updateCourseById_notFound_throwsException() {
        Course update = math111();
        assertThatThrownBy(()->courseService.updateCourseById(1000L, update)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course not found with id 1000");
    }

    @Test
    void updateCourseByCodeAndCourseNum_validData_updatesAndReturnsCourse() {
        Course course = math250();

        Course saved = courseService.createCourse(course);

        String course_code = saved.getCode();
        String course_num = saved.getCourseNum();

        Course updatedCourse = math111();

        Course updated = courseService.updateCourseByCodeAndCourseNum(course_code, course_num, updatedCourse);

        assertThat(updated).isNotNull();
        assertThat(updated.getTitle()).isEqualTo("Calculus 1");
        assertThat(updated.getCode()).isEqualTo("MATH");
        assertThat(updated.getCourseNum()).isEqualTo("111");
    }

    @Test
    void updateCourseByCodeAndCourseNum_notFound_throwsException() {
        Course update = math111();
        assertThatThrownBy(()->courseService.updateCourseByCodeAndCourseNum("MATH", "1111", update)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course MATH1111 not found");

    }

    @Test
    void deleteCourseById_validData_removesCourse() {
        Course course = math250();
        Course saved = courseService.createCourse(course);
        Long id = saved.getId();
        assertThat(courseService.getCourseById(id)).isNotNull();

        courseService.deleteCourseById(id);
        assertThatThrownBy(()-> courseService.getCourseById(id)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course not found with id " + id);
    }

    @Test
    void deleteCourseByCodeAndCourseNum_exists_removesCourse() {
        Course course = math250();
        Course saved = courseService.createCourse(course);
        String course_code = saved.getCode();
        String course_num = saved.getCourseNum();

        assertThat(courseService.getCourseByCodeAndCourseNum(course_code, course_num)).isNotNull();

        courseService.deleteCourseByCodeAndCourseNum(course_code, course_num);
        assertThatThrownBy(()->courseService.getCourseByCodeAndCourseNum(course_code, course_num)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course " + course_code+course_num+" not found");
    }

    @Test
    void deleteCourseByCodeAndCourseNum_notFound_throwsException() {
        assertThatThrownBy(()->courseService.getCourseByCodeAndCourseNum("MATH", "250")).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course MATH250 not found");
    }

    @Test
    void deleteCourseById_notFound_throwsException() {
        assertThatThrownBy(()-> courseService.getCourseById(1000L)).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course not found with id " + 1000L);

    }

    // PrerequisiteServiceTest for Courses

    @Test
    void setPrerequisites_validData_setsPrerequisites() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));

        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.AND,
                List.of(math111.getId(), math112.getId())
        );

        Prerequisite prereq = prerequisiteRepository.findByCourseId(math250.getId()).orElseThrow();
        assertThat(prereq.getType()).isEqualTo(PrerequisiteType.AND);
        assertThat(prereq.getItems()).hasSize(2);
        assertThat(prereq.getItems())
                .extracting(item -> item.getCourse().getId())
                .containsExactlyInAnyOrder(math111.getId(), math112.getId());

    }

    @Test
    void setPrerequisites_courseNotFound_throwsException() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));



        assertThatThrownBy(()-> courseService.setPrerequisites(
                10000L,
                PrerequisiteType.AND,
                List.of(math111.getId(), math112.getId())
        )).isInstanceOf(ResourceNotFoundException.class).hasMessage("Course not found with id 10000");
    }

    @Test
    void setPrerequisites_prerequisiteNotFound_throwsException() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));


        assertThatThrownBy(()-> courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.AND,
                List.of(math111.getId(), 10000L)
        )).isInstanceOf(ResourceNotFoundException.class).hasMessage("Required course not found with id 10000");
    }

    @Test
    void setPrerequisites_replaceExisting_updatesPrerequisites() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.AND,
                List.of(math111.getId(), math112.getId())
        );

        Prerequisite prereq = prerequisiteRepository.findByCourseId(math250.getId()).orElseThrow();
        assertThat(prereq.getType()).isEqualTo(PrerequisiteType.AND);
        assertThat(prereq.getItems()).hasSize(2);
        assertThat(prereq.getItems())
                .extracting(item -> item.getCourse().getId())
                .containsExactlyInAnyOrder(math111.getId(), math112.getId());


        Course math112z = courseService.createCourse(new Course("MATH", "114", "Calculus 2", "Derivations and Integrations", 3));
        Course math111z = courseService.createCourse(new Course("MATH", "113", "Calculus 1", "Intro to calculus", 3));

        courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.OR,
                List.of(math111z.getId(), math112z.getId())
        );

        Prerequisite prereq2 = prerequisiteRepository.findByCourseId(math250.getId()).orElseThrow();
        assertThat(prereq2.getType()).isEqualTo(PrerequisiteType.OR);
        assertThat(prereq2.getItems()).hasSize(2);
        assertThat(prereq2.getItems())
                .extracting(item -> item.getCourse().getId())
                .containsExactlyInAnyOrder(math111z.getId(), math112z.getId());
    }

    @Test
    void getPrerequisites_exists_returnsPrerequisites() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.AND,
                List.of(math111.getId(), math112.getId())
        );

        Prerequisite prereq = courseService.getPrerequisite(math250.getId());
        assertThat(prereq.getType()).isEqualTo(PrerequisiteType.AND);
        assertThat(prereq.getItems()).hasSize(2);
        assertThat(prereq.getItems())
            .extracting(item -> item.getCourse().getId())
                .containsExactlyInAnyOrder(math111.getId(), math112.getId());
    }

    @Test
    void getPrerequisites_none_returnsNull() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));

        Prerequisite prereq = courseService.getPrerequisite(math250.getId());

        assertThat(prereq).isNull();
    }

    @Test
    void deletePrerequisite_exists_removesPrerequisites() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        Course math112 = courseService.createCourse(new Course("MATH", "112", "Calculus 2", "Derivations and Integrations", 3));
        Course math111 = courseService.createCourse(new Course("MATH", "111", "Calculus 1", "Intro to calculus", 3));

        courseService.setPrerequisites(
                math250.getId(),
                PrerequisiteType.AND,
                List.of(math111.getId(), math112.getId())
        );

        assertThat(courseService.getPrerequisite(math250.getId())).isNotNull();

        courseService.removePrerequisite(math250.getId());
        assertThat(courseService.getPrerequisite(math250.getId())).isNull();
    }

    @Test
    void deletePrerequisite_none_doesNothing() {
        Course math250 = courseService.createCourse(new Course("MATH", "250", "Foundations of Mathematics", "Intro to theoretical mathematics", 3));
        assertThat(courseService.getPrerequisite(math250.getId())).isNull();

        courseService.removePrerequisite(math250.getId());

        assertThat(courseService.getPrerequisite(math250.getId())).isNull();

    }
}