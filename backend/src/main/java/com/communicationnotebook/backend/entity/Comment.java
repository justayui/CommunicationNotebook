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

/**
 * コメント情報を表すエンティティです。commentsテーブルと対応します。
 */
@Entity
@Table(name = "comments")
@Getter
@Setter
public class Comment {
    /**
     * 主キーとなるIDです。自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * コメントに紐づく投稿の情報です。コメントと投稿は多対一の関係です。未入力不可です。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    /**
     * コメントに紐づくユーザー情報です。コメントとユーザーは多対一の関係です。未入力不可です。
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * コメントの内容です。テキスト形式で入力します。未入力不可です。
     */
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 削除の有無です。未入力不可です。初期値はfalseです。
     */
    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    /**
     * コメント作成時間です。LocalDateTimeが自動で設定されます。
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
