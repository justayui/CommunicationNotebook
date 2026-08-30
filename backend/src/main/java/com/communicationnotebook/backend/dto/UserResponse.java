package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.User;

public record UserResponse(Integer id, String employeeId, String name, boolean admin) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmployeeId(), user.getName(), user.isAdmin());
    }
}
