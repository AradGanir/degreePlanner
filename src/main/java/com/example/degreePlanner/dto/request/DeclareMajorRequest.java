package com.example.degreePlanner.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DeclareMajorRequest {

    @NotBlank(message="Major code is required")
    private String majorCode;

    @NotBlank(message="Major designation required")
    private String majorDesignation;

    private boolean isPrimary = false;

    public DeclareMajorRequest() {}

    public DeclareMajorRequest(String majorCode, String majorDesignation, boolean isPrimary) {
        this.majorCode = majorCode;
        this.majorDesignation = majorDesignation;
        this.isPrimary = isPrimary;
    }

    public String getMajorCode() {return majorCode;}
    public void setMajorCode(String majorCode) {this.majorCode = majorCode;}

    public String getMajorDesignation() {return majorDesignation;}
    public void setMajorDesignation(String majorDesignation) {this.majorDesignation = majorDesignation;}

    public boolean isPrimary() {return isPrimary;}
    public void setPrimary(boolean isPrimary) {this.isPrimary = isPrimary;}
}



