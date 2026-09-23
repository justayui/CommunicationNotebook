package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Note;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * notesテーブルと紐づくリポジトリです。
 */
public interface NoteRepository extends JpaRepository<Note, Integer> {

    /**
     * 削除されていない投稿を新しい順に、投稿者の情報も含めて一括で取得します。
     *
     * @return 投稿情報一覧（全件）
     */
    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.deleted = false ORDER BY n.createdAt DESC")
    List<Note> findByDeletedFalseOrderByCreatedAtDesc();

    /**
     * 投稿IDに紐づく投稿の情報を、投稿者の情報も含めて取得します。該当する投稿が存在しない場合は空のOptionalを返します。
     *
     * @param id 投稿ID
     * @return IDに紐づく投稿
     */
    @Query("SELECT n FROM Note n JOIN FETCH n.user WHERE n.id = :id")
    Optional<Note> findByIdWithUser(Integer id);

    /**
     * 削除されていない投稿を、キーワード・カテゴリ・お気に入りで絞込検索します（投稿者の情報も含む）。
     *
     * @param keyword キーワード。部分一致で可。nullの場合は絞込を行わない。
     * @param category カテゴリ。完全一致。nullの場合は絞込を行わない。
     * @param favoriteOnly trueの場合、userIdのユーザーがお気に入り登録済みの投稿のみに絞り込む。
     * @param userId お気に入り絞込対象のユーザーID(favoriteOnlyがtrueの場合のみ使用)
     * @return 絞込検索結果（投稿時間が新しい順）
     */
    @Query(
            """
            SELECT n FROM Note n JOIN FETCH n.user
            WHERE n.deleted = false
              AND (:keyword IS NULL OR n.content LIKE CONCAT('%', CAST(:keyword AS string), '%'))
              AND (:category IS NULL OR n.category = CAST(:category AS string))
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
