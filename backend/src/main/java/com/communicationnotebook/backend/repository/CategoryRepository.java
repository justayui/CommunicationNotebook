package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * categoriesテーブルと紐づくリポジトリです。
 */
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    /**
     * カテゴリを全件取得しidカラムで昇順に並べ替え返却します。
     *
     * @return カテゴリ情報（全件）
     */
    List<Category> findAllByOrderByIdAsc();
}
