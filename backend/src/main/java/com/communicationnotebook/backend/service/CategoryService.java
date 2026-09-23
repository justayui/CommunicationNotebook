package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.CategoryResponse;
import com.communicationnotebook.backend.repository.CategoryRepository;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * カテゴリ情報を取り扱うサービスです。
 * カテゴリの検索を行います。
 */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    /**
     * カテゴリ情報の全件取得
     * リポジトリからID昇順で取得したカテゴリ情報を、CategoryResponse型のリストに変換して返却します。
     *
     * @return カテゴリ情報一覧（全件）
     */
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAllByOrderByIdAsc().stream()
                .map(CategoryResponse::from)
                .toList();
    }
}
