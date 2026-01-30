package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.Requirement;
import com.example.degreePlanner.entity.RequirementType;

import java.util.List;

public class RequirementResponse {
    private Long id;
    private String name;
    private String description;
    private RequirementType type;
    private Integer minCredits;
    private List<CourseResponse> courses;

    public RequirementResponse() {}

    public static RequirementResponse fromEntity(Requirement requirement) {
        RequirementResponse response = new RequirementResponse();
        response.id = requirement.getId();
        response.name = requirement.getName();
        response.description = requirement.getDescription();
        response.type = requirement.getType();
        response.minCredits = requirement.getMinCredits();
        response.courses = requirement.getCourses().stream()
                .map(CourseResponse::fromEntity)
                .toList();
        return response;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public RequirementType getType() { return type; }
    public Integer getMinCredits() { return minCredits; }
    public List<CourseResponse> getCourses() { return courses; }
}
