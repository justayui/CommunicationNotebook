package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.CategoryResponse;
import com.communicationnotebook.backend.entity.Category;
import com.communicationnotebook.backend.repository.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category newCategory(Integer id, String name) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        return category;
    }

    @Test
    void findAll_returnsCategoriesMappedToResponse() {
        when(categoryRepository.findAllByOrderByIdAsc())
                .thenReturn(List.of(newCategory(1, "手順変更"), newCategory(2, "委員会")));

        List<CategoryResponse> result = categoryService.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("手順変更");
        assertThat(result.get(1).name()).isEqualTo("委員会");
    }

    @Test
    void findAll_returnsEmptyList_whenNoCategoriesExist() {
        when(categoryRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        List<CategoryResponse> result = categoryService.findAll();

        assertThat(result).isEmpty();
    }
}
