package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteRepository extends JpaRepository<Note, Integer> {

    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.deleted = false ORDER BY n.createdAt DESC")
    List<Note> findByDeletedFalseOrderByCreatedAtDesc();

    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Note> findByIdWithUser(Integer id);

    @Query(
            """
            SELECT n FROM Note n JOIN FETCH n.user
            WHERE n.deleted = false
              AND (:keyword IS NULL OR n.content LIKE CONCAT('%', :keyword, '%'))
              AND (:category IS NULL OR n.category = :category)
              AND (:favoriteOnly = false OR n.id IN (
                    SELECT f.note.id FROM Favorite f WHERE f.user.id = :userId))
            ORDER BY n.createdAt DESC
            """)
    List<Note> search(
            @Param("keyword") String keyword,
            @Param("category") String category,
            @Param("favoriteOnly") boolean favoriteOnly,
            @Param("userId") Integer userId);
}
