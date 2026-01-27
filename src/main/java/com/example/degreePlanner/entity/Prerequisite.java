package com.example.degreePlanner.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
public class Prerequisite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="course_id", nullable = true, unique = true)
    private Course course;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PrerequisiteType type;

    @OneToMany(mappedBy = "prerequisite", cascade = CascadeType.ALL, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private Set<PrerequisiteItem> items = new HashSet<>();

    public Long getId() { return this.id; }


    public Course getCourse() { return this.course; }
    public PrerequisiteType getType() { return this.type; }

    public void setCourse(Course course) { this.course = course; }
    public void setType(PrerequisiteType type) { this.type = type; }

    public void setItems(Set<PrerequisiteItem> items) { this.items = items; }
    public Set<PrerequisiteItem> getItems() { return this.items; }

    public boolean isRoot() {return course!= null;}





    protected Prerequisite() {}
    public Prerequisite(Course course, PrerequisiteType type,  Set<PrerequisiteItem> items) {
        this.course = course;
        this.type = type;
        this.items = items;
    }


}
