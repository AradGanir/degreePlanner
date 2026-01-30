package com.example.degreePlanner.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "enrollment", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id", "semester"}))
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private Grade grade;

    @Column(nullable = false)
    private String semester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus enrollmentStatus;

    protected Enrollment() {}

    public Enrollment(Student student, Course course, String semester, EnrollmentStatus status) {
        this.student = student;
        this.course = course;
        this.semester = semester;
        this.enrollmentStatus = status;
    }

    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public Course getCourse() { return course; }
    public Grade getGrade() { return grade; }
    public String getSemester() { return semester; }
    public EnrollmentStatus getEnrollmentStatus() { return enrollmentStatus; }

    public void setStudent(Student student) { this.student = student; }
    public void setCourse(Course course) { this.course = course; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public void setSemester(String semester) { this.semester = semester; }
    public void setEnrollmentStatus(EnrollmentStatus status) { this.enrollmentStatus = status; }
}