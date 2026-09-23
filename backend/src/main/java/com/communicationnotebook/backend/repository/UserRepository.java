package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * usersテーブルと紐づくリポジトリです。
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 有効なユーザーの情報を全件取得します。
     * 
     * @return ユーザー情報一覧（全件）
     */
    List<User> findByDeletedFalse();

    /**
     * 職員IDに紐づくユーザー情報を取得します。該当ユーザーが存在しない場合は空のOptionalを返します。
     * 
     * @param employeeId 職員ID
     * @return 職員IDに紐づくユーザー情報
     */
    Optional<User> findByEmployeeId(String employeeId);

    /**
     * 職員IDに紐づくユーザーの存在の有無を確認します。
     * 
     * @param employeeId 職員ID
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    boolean existsByEmployeeId(String employeeId);

    /**
     * 管理者権限を持ち、かつ削除されていないユーザーが存在しているか確認します。
     * 
     * @return 存在する場合はtrue、存在しない場合はfalse
     */
    boolean existsByAdminTrueAndDeletedFalse();
}
