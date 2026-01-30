package com.example.degreePlanner.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CourseImportRequest {
    private String dept;
    private String num;
    private String title;
    private String description;
    private Integer credits;

    @JsonProperty("min_credits")
    private Integer minCredits;

    // Ignored fields: ger, requisites, cross_listed

    public CourseImportRequest() {}

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }

    public String getNum() { return num; }
    public void setNum(String num) { this.num = num; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Integer getMinCredits() { return minCredits; }
    public void setMinCredits(Integer minCredits) { this.minCredits = minCredits; }

    /**
     * Get effective credits - use minCredits if available, otherwise credits
     */
    public int getEffectiveCredits() {
        if (minCredits != null && minCredits > 0) {
            return minCredits;
        }
        return credits != null ? credits : 0;
    }
}
