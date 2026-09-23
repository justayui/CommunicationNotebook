package com.communicationnotebook.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import java.util.List;
import java.util.Set;
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

        upperCaseNote = persistNote("定例MTGの議事録です");
        lowerCaseNote = persistNote("meeting資料を共有します");
        unrelatedNote = persistNote("本日の連絡事項です");
        entityManager.flush();
        entityManager.clear();
    }

    private Note persistNote(String content) {
        Note note = new Note();
        note.setUser(user);
        note.setCategory("業務連絡");
        note.setContent(content);
        note.setDeleted(false);
        return entityManager.persist(note);
    }

    // 開発用DBの既存データを除外し、このテストで登録した投稿のIDだけを返す
    private List<Integer> searchTestNoteIds(String keyword) {
        Set<Integer> testNoteIds = Set.of(upperCaseNote.getId(), lowerCaseNote.getId(), unrelatedNote.getId());
        return noteRepository.search(keyword, null, false, user.getId()).stream()
                .map(Note::getId)
                .filter(testNoteIds::contains)
                .toList();
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
}
