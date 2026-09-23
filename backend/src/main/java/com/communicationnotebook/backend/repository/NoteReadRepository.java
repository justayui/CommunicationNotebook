package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.NoteRead;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * note_readsテーブルと紐づくリポジトリです。
 */
public interface NoteReadRepository extends JpaRepository<NoteRead, Integer> {

    /**
     * ユーザーID及び投稿IDに紐づく既読情報の有無を確認します。
     *
     * @param userId ユーザーID
     * @param noteId 投稿ID
     * @return 既読済みの場合true、既読未の場合false
     */
    boolean existsByUser_IdAndNote_Id(Integer userId, Integer noteId);

    /**
     * 投稿IDに紐づく既読者を取得します。
     *
     * @param noteId 投稿ID
     * @return 投稿IDに紐づく既読者一覧（既読時間が古い順）
     */
    @Query("SELECT r FROM NoteRead r JOIN FETCH r.user WHERE r.note.id = :noteId ORDER BY r.createdAt ASC")
    List<NoteRead> findByNote_IdOrderByCreatedAtAsc(@Param("noteId") Integer noteId);

    /**
     * ユーザーIDに紐づく既読済みの投稿ID一覧を取得します。
     * 各投稿に対して、既読済みかどうかを判定する際に使用します。
     *
     * @param userId ユーザーID
     * @return ユーザーIDに紐づく既読済み投稿IDの集合
     */
    @Query("SELECT r.note.id FROM NoteRead r WHERE r.user.id = :userId")
    Set<Integer> findNoteIdsByUserId(@Param("userId") Integer userId);

    /**
     * 投稿ごとの既読者数をカウントし、一覧で返却します。
     *
     * @param noteIds 投稿IDのリスト
     * @return 投稿ごとの既読者数一覧
     */
    @Query("SELECT r.note.id AS noteId, COUNT(r) AS count FROM NoteRead r "
            + "WHERE r.note.id IN :noteIds GROUP BY r.note.id")
    List<NoteReadCount> countByNoteIds(@Param("noteIds") List<Integer> noteIds);

    /**
     * 投稿ごとの既読者数集計結果を受け取るためのインターフェースです。
     */
    interface NoteReadCount {
        Integer getNoteId();

        Long getCount();
    }
}
