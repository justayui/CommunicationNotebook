package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * commentsテーブルと紐づくリポジトリです。
 */
public interface CommentRepository extends JpaRepository<Comment, Integer> {

    /**
     * 投稿ごとに紐づく削除されていないコメントを古い順に、投稿者の情報も含めて一括で取得します。
     * 
     * @param noteId 投稿ID
     * @return コメント情報一覧
     */
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.note.id = :noteId AND c.deleted = false ORDER BY c.createdAt ASC")
    List<Comment> findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(@Param("noteId") Integer noteId);

    /**
     * 投稿IDに紐づく特定のコメント情報を取得します。該当するコメントが存在しない場合は空のOptionalを返します。
     * 
     * @param id コメントID
     * @param noteId 投稿ID
     * @return 投稿IDに紐づく特定のコメント
     */
    Optional<Comment> findByIdAndNote_Id(Integer id, Integer noteId);

    /**
     * 投稿に紐づく有効なコメント数をカウントし、一覧で返却します。
     * 
     * @param noteIds 投稿IDのリスト
     * @return 投稿ごとのコメント数一覧
     */
    @Query(
            "SELECT c.note.id AS noteId, COUNT(c) AS count FROM Comment c "
                    + "WHERE c.deleted = false AND c.note.id IN :noteIds GROUP BY c.note.id")
    List<NoteCommentCount> countActiveByNoteIds(@Param("noteIds") List<Integer> noteIds);

    /**
     * 投稿ごとのコメント数集計結果を受け取るためのインターフェースです。
     */
    interface NoteCommentCount {
        Integer getNoteId();

        Long getCount();
    }
}
