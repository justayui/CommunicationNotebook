package com.communicationnotebook.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.communicationnotebook.backend.entity.Favorite;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
class NoteRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NoteRepository noteRepository;

    private User user;
    private User otherUser;
    private Note upperCaseNote;
    private Note lowerCaseNote;
    private Note unrelatedNote;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmployeeId("TEST-NOTE-REPO");
        user.setName("テスト太郎");
        user.setPassword("dummy");
        user.setAdmin(false);
        user.setDeleted(false);
        entityManager.persist(user);

        otherUser = new User();
        otherUser.setEmployeeId("TEST-NOTE-REPO-OTHER");
        otherUser.setName("テスト花子");
        otherUser.setPassword("dummy");
        otherUser.setAdmin(false);
        otherUser.setDeleted(false);
        entityManager.persist(otherUser);

        upperCaseNote = persistNote("定例MTGの議事録です");
        lowerCaseNote = persistNote("meeting資料を共有します");
        unrelatedNote = persistNote("本日の連絡事項です", "申し送り", false);

        // お気に入り絞込の検証用: userはlowerCaseNote、otherUserはupperCaseNoteをお気に入り登録する
        persistFavorite(user, lowerCaseNote);
        persistFavorite(otherUser, upperCaseNote);
        entityManager.flush();
        entityManager.clear();
    }

    private Note persistNote(String content) {
        return persistNote(content, false);
    }

    private Note persistNote(String content, boolean deleted) {
        return persistNote(content, "業務連絡", deleted);
    }

    private Note persistNote(String content, String category, boolean deleted) {
        Note note = new Note();
        note.setUser(user);
        note.setCategory(category);
        note.setContent(content);
        note.setDeleted(deleted);
        return entityManager.persist(note);
    }

    private void persistFavorite(User favoriteUser, Note note) {
        Favorite favorite = new Favorite();
        favorite.setUser(favoriteUser);
        favorite.setNote(note);
        entityManager.persist(favorite);
    }

    // created_atは登録時に自動で設定されるため、並び順を検証できるよう明示的な値に更新する
    private void updateCreatedAt(Note note, LocalDateTime createdAt) {
        entityManager.flush();
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE notes SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", note.getId())
                .executeUpdate();
    }

    // 開発用DBの既存データを除外し、このテストで登録した投稿のIDだけを返す
    private List<Integer> searchTestNoteIds(String keyword) {
        return searchTestNoteIds(keyword, null, false);
    }

    private List<Integer> searchTestNoteIds(String keyword, String category, boolean favoriteOnly) {
        Set<Integer> testNoteIds = Set.of(upperCaseNote.getId(), lowerCaseNote.getId(), unrelatedNote.getId());
        return noteRepository.search(keyword, category, favoriteOnly, user.getId()).stream()
                .map(Note::getId)
                .filter(testNoteIds::contains)
                .toList();
    }

    @Test
    void findByDeletedFalseOrderByCreatedAtDesc_excludesDeletedNotes_andSortsByCreatedAtDesc() {
        Note oldNote = persistNote("古い投稿です");
        Note newNote = persistNote("新しい投稿です");
        Note deletedNote = persistNote("削除済みの投稿です", true);
        updateCreatedAt(oldNote, LocalDateTime.of(2000, 1, 1, 9, 0));
        updateCreatedAt(newNote, LocalDateTime.of(2000, 1, 2, 9, 0));
        updateCreatedAt(deletedNote, LocalDateTime.of(2000, 1, 3, 9, 0));
        entityManager.clear();

        Set<Integer> testNoteIds = Set.of(oldNote.getId(), newNote.getId(), deletedNote.getId());
        List<Integer> actual = noteRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .map(Note::getId)
                .filter(testNoteIds::contains)
                .toList();

        assertThat(actual).containsExactly(newNote.getId(), oldNote.getId());
    }

    @Test
    void findByDeletedFalseOrderByCreatedAtDesc_fetchesUser() {
        Note actual = noteRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .filter(note -> note.getId().equals(upperCaseNote.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(Hibernate.isInitialized(actual.getUser())).isTrue();
        assertThat(actual.getUser().getName()).isEqualTo("テスト太郎");
    }

    @Test
    void findByIdWithUser_returnsNoteWithUser_whenNoteExists() {
        Optional<Note> actual = noteRepository.findByIdWithUser(upperCaseNote.getId());

        assertThat(actual).isPresent();
        assertThat(actual.get().getContent()).isEqualTo("定例MTGの議事録です");
        assertThat(Hibernate.isInitialized(actual.get().getUser())).isTrue();
        assertThat(actual.get().getUser().getName()).isEqualTo("テスト太郎");
    }

    @Test
    void findByIdWithUser_returnsEmpty_whenNoteDoesNotExist() {
        // IDは1から自動採番されるため、負の値は存在しない
        assertThat(noteRepository.findByIdWithUser(-1)).isEmpty();
    }

    @Test
    void search_matchesUpperCaseContent_whenKeywordIsLowerCase() {
        assertThat(searchTestNoteIds("mtg")).containsExactly(upperCaseNote.getId());
    }

    @Test
    void search_matchesLowerCaseContent_whenKeywordIsUpperCase() {
        assertThat(searchTestNoteIds("MEETING")).containsExactly(lowerCaseNote.getId());
    }

    @Test
    void search_matchesContent_whenKeywordIsMixedCase() {
        assertThat(searchTestNoteIds("MeEtInG")).containsExactly(lowerCaseNote.getId());
    }

    @Test
    void search_matchesContent_whenKeywordCaseIsSame() {
        assertThat(searchTestNoteIds("MTG")).containsExactly(upperCaseNote.getId());
    }

    @Test
    void search_returnsAllTestNotes_whenKeywordIsNull() {
        assertThat(searchTestNoteIds(null))
                .containsExactlyInAnyOrder(upperCaseNote.getId(), lowerCaseNote.getId(), unrelatedNote.getId());
    }

    @Test
    void search_returnsOnlyNotesOfCategory_whenCategoryIsSpecified() {
        assertThat(searchTestNoteIds(null, "申し送り", false)).containsExactly(unrelatedNote.getId());
    }

    @Test
    void search_returnsEmpty_whenNoNoteMatchesCategory() {
        assertThat(searchTestNoteIds(null, "該当なしカテゴリ", false)).isEmpty();
    }

    @Test
    void search_returnsOnlyNotesFavoritedByUser_whenFavoriteOnlyIsTrue() {
        // otherUserのお気に入り(upperCaseNote)は含まれない
        assertThat(searchTestNoteIds(null, null, true)).containsExactly(lowerCaseNote.getId());
    }

    @Test
    void search_appliesAllConditions_whenCategoryAndFavoriteOnlyAreSpecified() {
        // upperCaseNoteはカテゴリが一致するがお気に入り未登録、unrelatedNoteはカテゴリが不一致
        assertThat(searchTestNoteIds(null, "業務連絡", true)).containsExactly(lowerCaseNote.getId());
    }
}
