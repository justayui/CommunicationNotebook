package com.communicationnotebook.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.communicationnotebook.backend.entity.Comment;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository.NoteCommentCount;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

// 開発用PostgreSQLに接続して実行する。各テストはロールバックされるため、登録したデータはDBに残らない。
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CommentRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CommentRepository commentRepository;

    private User user;
    private Note note;
    private Note otherNote;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmployeeId("TEST-COMMENT-REPO");
        user.setName("テスト太郎");
        user.setPassword("dummy");
        user.setAdmin(false);
        user.setDeleted(false);
        entityManager.persist(user);

        note = persistNote("コメント対象の投稿です");
        otherNote = persistNote("別の投稿です");
    }

    private Note persistNote(String content) {
        Note newNote = new Note();
        newNote.setUser(user);
        newNote.setCategory("業務連絡");
        newNote.setContent(content);
        newNote.setDeleted(false);
        return entityManager.persist(newNote);
    }

    private Comment persistComment(Note targetNote, String content, boolean deleted) {
        Comment comment = new Comment();
        comment.setNote(targetNote);
        comment.setUser(user);
        comment.setContent(content);
        comment.setDeleted(deleted);
        return entityManager.persist(comment);
    }

    // created_atは登録時に自動で設定されるため、並び順を検証できるよう明示的な値に更新する
    private void updateCreatedAt(Comment comment, LocalDateTime createdAt) {
        entityManager.flush();
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE comments SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", comment.getId())
                .executeUpdate();
    }

    @Test
    void findByNote_IdAndDeletedFalseOrderByCreatedAtAsc_excludesDeletedComments_andSortsByCreatedAtAsc() {
        Comment newComment = persistComment(note, "新しいコメントです", false);
        Comment oldComment = persistComment(note, "古いコメントです", false);
        Comment deletedComment = persistComment(note, "削除済みのコメントです", true);
        persistComment(otherNote, "別の投稿へのコメントです", false);
        updateCreatedAt(newComment, LocalDateTime.of(2000, 1, 2, 9, 0));
        updateCreatedAt(oldComment, LocalDateTime.of(2000, 1, 1, 9, 0));
        updateCreatedAt(deletedComment, LocalDateTime.of(1999, 12, 31, 9, 0));
        entityManager.clear();

        List<Comment> actual = commentRepository.findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(note.getId());

        assertThat(actual).extracting(Comment::getId).containsExactly(oldComment.getId(), newComment.getId());
    }

    @Test
    void findByNote_IdAndDeletedFalseOrderByCreatedAtAsc_fetchesUser() {
        persistComment(note, "コメントです", false);
        entityManager.flush();
        entityManager.clear();

        List<Comment> actual = commentRepository.findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(note.getId());

        assertThat(actual).hasSize(1);
        assertThat(Hibernate.isInitialized(actual.get(0).getUser())).isTrue();
        assertThat(actual.get(0).getUser().getName()).isEqualTo("テスト太郎");
    }

    @Test
    void countActiveByNoteIds_countsOnlyActiveComments_andOmitsNotesWithoutActiveComments() {
        Note noCommentNote = persistNote("コメントがない投稿です");
        Note onlyDeletedCommentNote = persistNote("削除済みのコメントだけがある投稿です");
        persistComment(note, "コメント1です", false);
        persistComment(note, "コメント2です", false);
        persistComment(note, "削除済みのコメントです", true);
        persistComment(otherNote, "別の投稿へのコメントです", false);
        persistComment(onlyDeletedCommentNote, "削除済みのコメントです", true);
        entityManager.flush();
        entityManager.clear();

        Map<Integer, Long> actual = commentRepository
                .countActiveByNoteIds(List.of(
                        note.getId(), otherNote.getId(), noCommentNote.getId(), onlyDeletedCommentNote.getId()))
                .stream()
                .collect(Collectors.toMap(NoteCommentCount::getNoteId, NoteCommentCount::getCount));

        // 有効なコメントがない投稿は集計結果に含まれない
        assertThat(actual).containsOnly(entry(note.getId(), 2L), entry(otherNote.getId(), 1L));
    }
}
