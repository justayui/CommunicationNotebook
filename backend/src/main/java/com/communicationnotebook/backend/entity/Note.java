package com.communicationnotebook.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * 投稿の情報を表すエンティティです。notesテーブルと対応します。
 */
@Entity
@Table(name = "notes")
@Getter
@Setter
public class Note {
    /**
     * 主キーとなるIDです。自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 投稿に紐づくユーザー情報です。投稿とユーザーは多対一の関係です。未入力不可です。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 投稿の内容です。テキスト形式で入力します。未入力不可です。
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * カテゴリです。未入力不可です。
     */
    @Column(name = "category", nullable = false)
    private String category;

    /**
     * 削除の有無です。未入力不可です。初期値はfalseです。
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    /**
     * 投稿作成時間です。LocalDateTimeが自動で設定されます。
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 投稿更新時間です。LocalDateTimeが自動で設定されます。
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
