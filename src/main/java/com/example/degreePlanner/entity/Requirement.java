package com.example.degreePlanner.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Requirement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "major_id", nullable = false)
    private Major major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequirementType type;

    @Column(nullable = false)
    private String name;

    private Integer minCredits;

    private String description;

    @ManyToMany
    @JoinTable(
            name = "requirement_course",
            joinColumns = @JoinColumn(name = "requirement_id"),
            inverseJoinColumns = @JoinColumn(name = "course_id")
    )
    private Set<Course> courses = new HashSet<>();

    protected Requirement() {}

    public Requirement(Major major, RequirementType type, String name, Integer minCredits, String description) {
        this.major = major;
        this.type = type;
        this.name = name;
        this.minCredits = minCredits;
        this.description = description;
    }

    public Long getId() { return id; }

    public Major getMajor() { return major; }
    public void setMajor(Major major) { this.major = major; }

    public RequirementType getType() { return type; }
    public void setType(RequirementType type) { this.type = type; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getMinCredits() { return minCredits; }
    public void setMinCredits(Integer minCredits) { this.minCredits = minCredits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Course> getCourses() { return courses; }
    public void setCourses(Set<Course> courses) { this.courses = courses; }

}
