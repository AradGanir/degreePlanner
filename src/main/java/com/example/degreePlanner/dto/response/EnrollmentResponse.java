package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.Enrollment;
import com.example.degreePlanner.entity.EnrollmentStatus;
import com.example.degreePlanner.entity.Grade;

public class EnrollmentResponse {
    private Long id;
    private String courseCode;
    private String courseNum;
    private String courseTitle;
    private Grade grade;
    private String semester;
    private EnrollmentStatus status;

    public EnrollmentResponse() {}

    public static EnrollmentResponse fromEntity(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.id = enrollment.getId();
        response.courseCode = enrollment.getCourse().getCode();
        response.courseNum = enrollment.getCourse().getCourseNum();
        response.courseTitle = enrollment.getCourse().getTitle();
        response.grade = enrollment.getGrade();
        response.semester = enrollment.getSemester();
        response.status = enrollment.getEnrollmentStatus();
        return response;
    }

    public Long getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public String getCourseNum() { return courseNum; }
    public String getCourseTitle() { return courseTitle; }
    public Grade getGrade() { return grade; }
    public String getSemester() { return semester; }
    public EnrollmentStatus getStatus() { return status; }
}
