package com.communicationnotebook.backend.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

import com.communicationnotebook.backend.config.SecurityConfig;
import com.communicationnotebook.backend.dto.CategoryResponse;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    private UserPrincipal principal() {
        User user = new User();
        user.setId(1);
        user.setEmployeeId("E001");
        user.setName("テスト太郎");
        user.setPassword("hashed");
        user.setAdmin(false);
        user.setDeleted(false);
        return new UserPrincipal(user);
    }

    @Test
    void findAll_returnsCategoryList() {
        when(categoryService.findAll())
                .thenReturn(List.of(new CategoryResponse(1, "手順変更"), new CategoryResponse(2, "委員会")));

        mockMvc.get().uri("/api/categories")
                .with(user(principal()))
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("手順変更");
    }

    @Test
    void findAll_returnsUnauthorized_whenNotAuthenticated() {
        mockMvc.get().uri("/api/categories").assertThat().hasStatus(401);
    }
}
