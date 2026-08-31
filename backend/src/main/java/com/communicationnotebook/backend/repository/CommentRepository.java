package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.note.id = :noteId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(@Param("noteId") Integer noteId);

    Optional<Comment> findByIdAndNote_Id(Integer id, Integer noteId);

    @Query(
            "SELECT c.note.id AS noteId, COUNT(c) AS count FROM Comment c "
                    + "WHERE c.deleted = false AND c.note.id IN :noteIds GROUP BY c.note.id")
    List<NoteCommentCount> countActiveByNoteIds(@Param("noteIds") List<Integer> noteIds);

    interface NoteCommentCount {
        Integer getNoteId();

        Long getCount();
    }
}
