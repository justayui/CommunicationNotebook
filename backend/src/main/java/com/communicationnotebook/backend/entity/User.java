package com.communicationnotebook.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * ユーザー情報を表すエンティティです。usersテーブルと対応します。
 */
@Entity
@Table(name = "users")
@Getter
@Setter
public class User {
    /**
     * 主キーとなるIDです。自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 従業員IDです。未入力不可です。一意である必要があります。
     * ログイン時のIDはこちらを使用します。
     */
    @Column(name = "employee_id", unique = true, nullable = false)
    private String employeeId;

    /**
     * ユーザー名です。未入力不可です。
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * 暗号化されたパスワードです。未入力不可です。
     */
    @Column(name = "password", nullable = false)
    private String password;

    /**
     * 管理者権限の有無です。未入力不可です。初期値はfalseです。
     */
    @Column(name = "is_admin", nullable = false)
    private boolean admin;

    /**
     * 削除の有無です。未入力不可です。初期値はfalseです。
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;
}
