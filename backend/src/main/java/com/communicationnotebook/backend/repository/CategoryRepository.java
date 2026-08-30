package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Category;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    List<Category> findAllByOrderByIdAsc();
}
