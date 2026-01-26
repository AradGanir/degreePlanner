package com.example.degreePlanner.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "course", uniqueConstraints = @UniqueConstraint(columnNames = {"code", "courseNum"}))
public class Course {
    protected Course(){}

    public Course(String code, int courseNum, String title, String description, int credits) {
        this.code = code;
        this.courseNum = courseNum;
        this.title = title;
        this.description = description;
        this.credits = credits;
    }


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return this.id; }

    @Column(nullable = false)
    private String code;
    public String getCode() { return this.code; }
    public void setCode (String code) { this.code = code; }

    @Column(nullable=false)
    private int courseNum;
    public int getCourseNum() { return this.courseNum; }
    public void setCourseNum (int courseNum) { this.courseNum = courseNum; }

    @Column(nullable = false)
    private String title;
    public String getTitle() { return this.title; }
    public void setTitle(String title) { this.title = title; }

    @Column(nullable = false)
    private String description;
    public String getDescription() { return this.description; }
    public void setDescription(String description) { this.description = description; }

    @Column(nullable = false)
    private int credits;  // max credits (or fixed if no range)
    public int getCredits() { return this.credits; }
    public void setCredits(int credits) { this.credits = credits; }

    @Column
    private Integer minCredits;  // only set if variable credits (e.g., 1-4)
    public Integer getMinCredits() { return this.minCredits; }
    public void setMinCredits(Integer minCredits) { this.minCredits = minCredits; }

    @Transient
    public boolean hasVariableCredits() { return minCredits != null && minCredits < credits; }

    @Transient
    public String getFullCode() {
        return code + courseNum;
    }

    @ManyToMany(mappedBy="courses")
    private Set<Requirement> requirements = new HashSet<>();




}
