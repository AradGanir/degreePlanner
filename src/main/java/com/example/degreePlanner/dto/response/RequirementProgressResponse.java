package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.entity.RequirementStatus;
import com.example.degreePlanner.service.ProgressService;

import java.util.List;

public class RequirementProgressResponse {
    private RequirementResponse requirement;
    private RequirementStatus status;
    private List<CourseResponse> completedCourses;
    private List<CourseResponse> remainingCourses;
    private int creditsCompleted;
    private int creditsRemaining;

    public RequirementProgressResponse() {}

    public static RequirementProgressResponse fromServiceResult(ProgressService.RequirementProgress progress) {
        RequirementProgressResponse response = new RequirementProgressResponse();
        response.requirement = RequirementResponse.fromEntity(progress.requirement());
        response.status = progress.status();
        response.completedCourses = progress.completedCourses().stream()
                .map(CourseResponse::fromEntity)
                .toList();
        response.remainingCourses = progress.remainingCourses().stream()
                .map(CourseResponse::fromEntity)
                .toList();
        response.creditsCompleted = progress.creditsCompleted();
        response.creditsRemaining = progress.creditsRemaining();
        return response;
    }

    public RequirementResponse getRequirement() { return requirement; }
    public RequirementStatus getStatus() { return status; }
    public List<CourseResponse> getCompletedCourses() { return completedCourses; }
    public List<CourseResponse> getRemainingCourses() { return remainingCourses; }
    public int getCreditsCompleted() { return creditsCompleted; }
    public int getCreditsRemaining() { return creditsRemaining; }
}
