package com.example.degreePlanner.dto.request;

import com.example.degreePlanner.entity.PrerequisiteType;

import java.util.List;

/**
 * Represents either a leaf (single course) or a nested group.
 *
 * Leaf example: { "courseId": 5 }
 * Group example: { "type": "OR", "items": [{ "courseId": 1 }, { "courseId": 2 }] }
 */
public class PrerequisiteItemRequest {

    // For leaf nodes - just a course ID
    private Long courseId;

    // For nested groups - type and nested items
    private PrerequisiteType type;
    private List<PrerequisiteItemRequest> items;

    public PrerequisiteItemRequest() {}

    // Leaf constructor
    public PrerequisiteItemRequest(Long courseId) {
        this.courseId = courseId;
    }

    // Group constructor
    public PrerequisiteItemRequest(PrerequisiteType type, List<PrerequisiteItemRequest> items) {
        this.type = type;
        this.items = items;
    }

    public boolean isLeaf() {
        return courseId != null;
    }

    public boolean isGroup() {
        return type != null && items != null;
    }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public PrerequisiteType getType() { return type; }
    public void setType(PrerequisiteType type) { this.type = type; }

    public List<PrerequisiteItemRequest> getItems() { return items; }
    public void setItems(List<PrerequisiteItemRequest> items) { this.items = items; }
}
