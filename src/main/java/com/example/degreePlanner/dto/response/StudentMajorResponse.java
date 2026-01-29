package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.StudentMajor;

import java.time.LocalDate;

public class StudentMajorResponse {

    private Long id;
    private String majorCode;
    private String majorDesignation;
    private String majorName;
    private LocalDate declaredDate;
    private Boolean isPrimary;


    public StudentMajorResponse() {}

    public static StudentMajorResponse fromEntity(StudentMajor studentMajor) {
        StudentMajorResponse response = new StudentMajorResponse(

        );
        response.id = studentMajor.getId();
        response.majorCode = studentMajor.getMajor().getCode();
        response.majorDesignation = studentMajor.getMajor().getDesignation();
        response.majorName = studentMajor.getMajor().getName();
        response.declaredDate = studentMajor.getDeclaredDate();
        response.isPrimary = studentMajor.getIsPrimary();

        return response;
    }

    public Long getId() { return id; }
    public String getMajorCode() { return majorCode; }
    public String getMajorDesignation() { return majorDesignation; }
    public String getMajorName() { return majorName; }
    public LocalDate getDeclaredDate() { return declaredDate; }
    public Boolean getIsPrimary() { return isPrimary; }


}
