package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Favorite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    boolean existsByUser_IdAndNote_Id(Integer userId, Integer noteId);

    Optional<Favorite> findByUser_IdAndNote_Id(Integer userId, Integer noteId);
}
