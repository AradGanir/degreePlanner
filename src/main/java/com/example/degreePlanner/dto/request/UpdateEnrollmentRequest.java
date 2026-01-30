package com.example.degreePlanner.dto.request;

import com.example.degreePlanner.entity.EnrollmentStatus;
import com.example.degreePlanner.entity.Grade;

public class UpdateEnrollmentRequest {

    private Grade grade;

    private EnrollmentStatus status;

    public UpdateEnrollmentRequest() {}

    public UpdateEnrollmentRequest(Grade grade, EnrollmentStatus status) {
        this.grade = grade;
        this.status = status;
    }

    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }

    public EnrollmentStatus getStatus() { return status; }
    public void setStatus(EnrollmentStatus status) { this.status = status; }
}
