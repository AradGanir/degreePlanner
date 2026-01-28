package com.example.degreePlanner.entity;
import jakarta.persistence.*;


@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return this.id; }

    @Column(unique = true, nullable = false)
    private String studentId;
    public String getStudentId() { return this.studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    @Column(unique = false, nullable = false)
    private String firstName;
    public String getFirstName() { return this.firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    @Column(unique = false, nullable = false)
    private String lastName;
    public String getLastName() { return this.lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    @Column(unique = true, nullable = false)
    private String email;
    public String getEmail() { return this.email; }
    public void setEmail(String email) { this.email = email; }

    protected Student() {}
    public Student(String studentId, String firstName, String lastName, String email) {
        this.studentId = studentId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
}
