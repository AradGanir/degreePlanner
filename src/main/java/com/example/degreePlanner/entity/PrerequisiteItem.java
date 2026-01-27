package com.example.degreePlanner.entity;

import jakarta.persistence.*;

@Entity
public class PrerequisiteItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @com.fasterxml.jackson.annotation.JsonBackReference
    @JoinColumn(name="prerequisite_id", nullable = false)
    private Prerequisite prerequisite;


    @ManyToOne
    @JoinColumn(name="required_course_id", nullable = true)
    private Course course;


    @ManyToOne
    @JoinColumn(name="nested_prerequisite_id", nullable = true)

    private Prerequisite nestedPrerequisite;


    public Long getId() { return id; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Prerequisite getPrerequisite() { return prerequisite; }
    public void setPrerequisite(Prerequisite prerequisite) {this.prerequisite = prerequisite;}

    public Prerequisite getNestedPrerequisite() {return nestedPrerequisite;}

    public void setNestedPrerequisite(Prerequisite nestedPrerequisite) {this.nestedPrerequisite = nestedPrerequisite;}

    public boolean isLeaf() {return course != null;}
    public boolean isGroup() {return nestedPrerequisite != null;}

    protected PrerequisiteItem() {}
    public PrerequisiteItem(Prerequisite prerequisite, Course course){
        this.prerequisite = prerequisite;
        this.course = course;
    }

    public PrerequisiteItem(Prerequisite prerequisite, Prerequisite nestedPrerequisite){
        this.prerequisite = prerequisite;
        this.nestedPrerequisite = nestedPrerequisite;
    }
}
