package com.communicationnotebook.backend.dto;

import com.communicationnotebook.backend.entity.User;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ユーザー情報を表すDTOです。")
public record UserResponse(
    @Schema(description = "ユーザーのIDです。", example = "1")
    Integer id,
    @Schema(description = "ユーザーの従業員IDです。", example = "E001")
    String employeeId, 
    @Schema(description = "ユーザー名です。", example = "田中花子")
    String name, 
    @Schema(description = "管理者権限の有無です。", example = "false")
    boolean admin
    ) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmployeeId(), user.getName(), user.isAdmin());
    }
}
