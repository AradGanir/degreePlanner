package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.Course;

public class CourseResponse {
    private Long id;
    private String code;
    private String courseNum;
    private String title;
    private String description;
    private int credits;

    public CourseResponse() {}

    public static CourseResponse fromEntity(Course course) {
        CourseResponse response = new CourseResponse();
        response.id = course.getId();
        response.code = course.getCode();
        response.courseNum = course.getCourseNum();
        response.title = course.getTitle();
        response.description = course.getDescription();
        response.credits = course.getCredits();
        return response;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getCourseNum() { return courseNum; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getCredits() { return credits; }
}
