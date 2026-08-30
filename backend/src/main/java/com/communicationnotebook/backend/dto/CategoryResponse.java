package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Category;

public record CategoryResponse(Integer id, String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
