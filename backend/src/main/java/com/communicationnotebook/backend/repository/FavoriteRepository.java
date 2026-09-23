package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.Favorite;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * favoritesテーブルと紐づくリポジトリです。
 */
public interface FavoriteRepository extends JpaRepository<Favorite, Integer> {

    /**
     * ユーザーID及び投稿IDに紐づくお気に入り登録の有無を確認します。
     * お気に入り登録時、重複防止のための事前チェックに使用されます。
     * 
     * @param userId ユーザーID
     * @param noteId 投稿ID
     * @return お気に入り登録有の場合はtrue、無の場合はfalse
     */
    boolean existsByUser_IdAndNote_Id(Integer userId, Integer noteId);

    /**
     * ユーザーID及び投稿IDに紐づくお気に入り情報を取得します。該当するお気に入り情報が存在しない場合は空のoptionalを返します。
     * お気に入り解除時に使用されます。
     * 
     * @param userId ユーザーID
     * @param noteId 投稿ID
     * @return ユーザーID及び投稿IDに紐づくお気に入り情報
     */
    Optional<Favorite> findByUser_IdAndNote_Id(Integer userId, Integer noteId);

    /**
     * ユーザーIDに紐づくお気に入り登録済みの投稿ID一覧を取得します。
     * 各投稿に対して、お気に入り登録済みかどうかを判定する際に使用します。
     * 
     * @param userId ユーザーID
     * @return ユーザーIDに紐づくお気に入り登録済みの投稿IDの集合
     */
    @Query("SELECT f.note.id FROM Favorite f WHERE f.user.id = :userId")
    Set<Integer> findNoteIdsByUserId(@Param("userId") Integer userId);
}
