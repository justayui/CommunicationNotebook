package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.NoteRead;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoteReadRepository extends JpaRepository<NoteRead, Integer> {

    boolean existsByUser_IdAndNote_Id(Integer userId, Integer noteId);

    @Query("SELECT r.note.id FROM NoteRead r WHERE r.user.id = :userId")
    Set<Integer> findNoteIdsByUserId(@Param("userId") Integer userId);

    @Query("SELECT r.note.id AS noteId, COUNT(r) AS count FROM NoteRead r "
            + "WHERE r.note.id IN :noteIds GROUP BY r.note.id")
    List<NoteReadCount> countByNoteIds(@Param("noteIds") List<Integer> noteIds);

    interface NoteReadCount {
        Integer getNoteId();

        Long getCount();
    }
}
