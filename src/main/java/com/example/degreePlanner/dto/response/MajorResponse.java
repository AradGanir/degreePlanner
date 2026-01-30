package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.Major;

public class MajorResponse {
    private Long id;
    private String name;
    private String code;
    private String designation;
    private String description;
    private int totalCreditsRequired;

    public MajorResponse() {}

    public static MajorResponse fromEntity(Major major) {
        MajorResponse response = new MajorResponse();
        response.id = major.getId();
        response.name = major.getName();
        response.code = major.getCode();
        response.designation = major.getDesignation();
        response.description = major.getDescription();
        response.totalCreditsRequired = major.getTotalCreditsRequired();
        return response;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
    public String getDesignation() { return designation; }
    public String getDescription() { return description; }
    public int getTotalCreditsRequired() { return totalCreditsRequired; }
}
