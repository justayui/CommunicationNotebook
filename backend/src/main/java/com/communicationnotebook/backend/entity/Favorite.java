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

/**
 * お気に入り情報を表すエンティティです。favoritesテーブルと紐づくリポジトリです。
 */
@Entity
@Table(name = "favorites")
@Getter
@Setter
public class Favorite {
    /**
     * 主キーとなるIDです。自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * ユーザー情報です。お気に入りとユーザーは多対一の関係です。未入力不可です。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * 投稿情報です。お気に入りと投稿は多対一の関係です。未入力不可です。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    /**
     * お気に入り登録時間です。LocalDateTimeが自動で設定されます。
     */
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
