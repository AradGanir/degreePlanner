package com.example.degreePlanner.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "student_major", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "major_id"}))
public class StudentMajor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name="major_id", nullable=false)
    private Major major;


    @Column(name="declared_date")
    private LocalDate declaredDate;

    @Column(name="is_primary")
    private Boolean isPrimary = false;

    public Long getId() {return this.id;}
    public Student getStudent() {return this.student;}
    public Major getMajor() {return this.major;}
    public LocalDate getDeclaredDate() {return this.declaredDate;}

    public void setStudent(Student student) {this.student = student;}
    public void setMajor(Major major) {this.major = major;}
    public void setDeclaredDate(LocalDate declaredDate) {this.declaredDate = declaredDate;}
    public void setIsPrimary(Boolean isPrimary) {this.isPrimary = isPrimary;}

    protected StudentMajor() {}
    public StudentMajor(Student student, Major major, LocalDate declaredDate, Boolean isPrimary) {
        this.student = student;
        this.major = major;
        this.declaredDate = declaredDate;
        this.isPrimary = isPrimary;
    }


    public Boolean getIsPrimary() {
        return isPrimary;
    }
}
