package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.Student;

public class StudentResponse {
    private Long id;
    private String studentId;
    private String firstName;
    private String lastName;
    private String email;

    public StudentResponse() {}

    public StudentResponse(Long id, String studentId, String firstName, String lastName, String email) {
        this.id = id;
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public static StudentResponse fromEntity(Student student) {
        return new StudentResponse(
            student.getId(),
            student.getStudentId(),
            student.getFirstName(),
            student.getLastName(),
            student.getEmail()
        );
    }

    public Long getId() { return this.id; }
    public String getStudentId() { return this.studentId; }
    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getEmail() { return this.email; }
}
