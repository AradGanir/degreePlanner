package com.example.degreePlanner.dto.request;

import jakarta.validation.constraints.NotBlank;

public class CreateEnrollmentRequest {

    @NotBlank(message = "Course code is required")
    private String courseCode;

    @NotBlank(message = "Course number is required")
    private String courseNum;

    @NotBlank(message = "Semester is required")
    private String semester;

    public CreateEnrollmentRequest() {}

    public CreateEnrollmentRequest(String courseCode, String courseNum, String semester) {
        this.courseCode = courseCode;
        this.courseNum = courseNum;
        this.semester = semester;
    }

    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }

    public String getCourseNum() { return courseNum; }
    public void setCourseNum(String courseNum) { this.courseNum = courseNum; }

    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
}
