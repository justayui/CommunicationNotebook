package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Favorite;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    boolean existsByUser_IdAndNote_Id(Integer userId, Integer noteId);

    Optional<Favorite> findByUser_IdAndNote_Id(Integer userId, Integer noteId);

    @Query("SELECT f.note.id FROM Favorite f WHERE f.user.id = :userId")
    Set<Integer> findNoteIdsByUserId(@Param("userId") Integer userId);
}
