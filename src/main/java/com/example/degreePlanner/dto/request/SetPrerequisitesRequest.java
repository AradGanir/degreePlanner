package com.example.degreePlanner.dto.request;

import com.example.degreePlanner.entity.PrerequisiteType;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Request to set complex nested prerequisites for a course.
 *
 * Example for: (QTM 110 OR QTM_OX 110) AND (QTM 210 OR MATH 361) AND MATH 221
 *
 * {
 *   "type": "AND",
 *   "items": [
 *     { "type": "OR", "items": [{ "courseId": 1 }, { "courseId": 2 }] },
 *     { "type": "OR", "items": [{ "courseId": 3 }, { "courseId": 4 }] },
 *     { "courseId": 5 }
 *   ]
 * }
 */
public class SetPrerequisitesRequest {

    @NotNull(message = "Prerequisite type is required")
    private PrerequisiteType type;

    @NotNull(message = "Items are required")
    private List<PrerequisiteItemRequest> items;

    public SetPrerequisitesRequest() {}

    public SetPrerequisitesRequest(PrerequisiteType type, List<PrerequisiteItemRequest> items) {
        this.type = type;
        this.items = items;
    }

    public PrerequisiteType getType() { return type; }
    public void setType(PrerequisiteType type) { this.type = type; }

    public List<PrerequisiteItemRequest> getItems() { return items; }
    public void setItems(List<PrerequisiteItemRequest> items) { this.items = items; }
}
