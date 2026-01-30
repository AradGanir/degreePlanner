package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.Course;

import java.util.List;

public class EligibilityResponse {
    private Long courseId;
    private String courseCode;
    private String courseNum;
    private boolean eligible;
    private List<CourseResponse> missingPrerequisites;

    public EligibilityResponse() {}

    public EligibilityResponse(Long courseId, String courseCode, String courseNum, boolean eligible, List<CourseResponse> missingPrerequisites) {
        this.courseId = courseId;
        this.courseCode = courseCode;
        this.courseNum = courseNum;
        this.eligible = eligible;
        this.missingPrerequisites = missingPrerequisites;
    }

    public static EligibilityResponse fromCourse(Course course, boolean eligible, List<Course> missing) {
        List<CourseResponse> missingResponses = missing.stream()
                .map(CourseResponse::fromEntity)
                .toList();

        return new EligibilityResponse(
                course.getId(),
                course.getCode(),
                course.getCourseNum(),
                eligible,
                missingResponses
        );
    }

    public Long getCourseId() { return courseId; }
    public String getCourseCode() { return courseCode; }
    public String getCourseNum() { return courseNum; }
    public boolean isEligible() { return eligible; }
    public List<CourseResponse> getMissingPrerequisites() { return missingPrerequisites; }
}
