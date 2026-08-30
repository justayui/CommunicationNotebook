package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Note;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NoteRepository extends JpaRepository<Note, Integer> {

    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.deleted = false ORDER BY n.createdAt DESC")
    List<Note> findByDeletedFalseOrderByCreatedAtDesc();
}
