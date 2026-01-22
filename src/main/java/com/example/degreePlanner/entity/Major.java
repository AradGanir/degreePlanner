package com.example.degreePlanner.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table
public class Major {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public Long getId() { return id; }


    @Column(nullable = false, unique=true)
    private String name;
    public String getName() { return this.name; }
    public void setName (String name) { this.name = name; }

    @Column(nullable=false, unique=true)
    private String code;
    public String getCode() { return this.code; }
    public void setCode (String code) { this.code = code; }

    @Column
    private String designation;
    public String getDesignation() { return this.designation; }
    public void setDesignation (String designation) { this.designation = designation; }


    @Column
    private String description;
    public String getDescription() { return this.description; }
    public void setDescription (String description) { this.description = description; }


    @Column(nullable=false)
    @NotNull
    private int totalCreditsRequired;
    public int getTotalCreditsRequired() { return this.totalCreditsRequired; }
    public void setTotalCreditsRequired (int totalCreditsRequired) { this.totalCreditsRequired = totalCreditsRequired; }

    protected Major(){}

    public Major(String name, String code, String designation, String description, int totalCreditsRequired) {
        this.name = name;
        this.code = code;
        this.designation = designation;
        this.description = description;
        this.totalCreditsRequired = totalCreditsRequired;
    }



}
