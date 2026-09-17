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
 * カテゴリ情報を表すエンティティです。categoriesテーブルと対応します。
 */
@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {
    /**
     * カテゴリのIDです。自動採番されます。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * カテゴリ名です。未入力不可です。一意である必要があります。
     */
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
