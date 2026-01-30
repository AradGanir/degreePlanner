package com.example.degreePlanner.dto.response;

import com.example.degreePlanner.service.ProgressService;

import java.util.List;

public class MajorProgressResponse {
    private MajorResponse major;
    private int totalCreditsRequired;
    private int creditsCompleted;
    private int creditsRemaining;
    private double percentComplete;
    private List<RequirementProgressResponse> requirements;

    public MajorProgressResponse() {}

    public static MajorProgressResponse fromServiceResult(ProgressService.MajorProgress progress) {
        MajorProgressResponse response = new MajorProgressResponse();
        response.major = MajorResponse.fromEntity(progress.major());
        response.totalCreditsRequired = progress.totalCreditsRequired();
        response.creditsCompleted = progress.creditsCompleted();
        response.creditsRemaining = progress.creditsRemaining();
        response.percentComplete = progress.percentComplete();
        response.requirements = progress.requirements().stream()
                .map(RequirementProgressResponse::fromServiceResult)
                .toList();
        return response;
    }

    public MajorResponse getMajor() { return major; }
    public int getTotalCreditsRequired() { return totalCreditsRequired; }
    public int getCreditsCompleted() { return creditsCompleted; }
    public int getCreditsRemaining() { return creditsRemaining; }
    public double getPercentComplete() { return percentComplete; }
    public List<RequirementProgressResponse> getRequirements() { return requirements; }
}
