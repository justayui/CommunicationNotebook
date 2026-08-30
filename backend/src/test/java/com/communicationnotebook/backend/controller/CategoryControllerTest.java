package com.communicationnotebook.backend.controller;

import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.CategoryResponse;
import com.communicationnotebook.backend.service.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void findAll_returnsCategoryList() {
        when(categoryService.findAll())
                .thenReturn(List.of(new CategoryResponse(1, "手順変更"), new CategoryResponse(2, "委員会")));

        mockMvc.get().uri("/api/categories")
                .assertThat()
                .hasStatusOk()
                .bodyJson()
                .extractingPath("$[0].name")
                .isEqualTo("手順変更");
    }
}
