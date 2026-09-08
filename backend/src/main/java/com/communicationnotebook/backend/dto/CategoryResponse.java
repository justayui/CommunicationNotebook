package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.Category;

public record CategoryResponse(String name) {

    public static CategoryResponse from(Category category) {
        return new CategoryResponse(category.getName());
    }
}
