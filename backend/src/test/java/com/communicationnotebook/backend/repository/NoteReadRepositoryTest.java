package com.communicationnotebook.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.NoteRead;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteReadRepository.NoteReadCount;
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
class NoteReadRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NoteReadRepository noteReadRepository;

    private User firstReader;
    private User secondReader;
    private User nonReader;
    private Note note;
    private Note otherNote;

    @BeforeEach
    void setUp() {
        firstReader = persistUser("TEST-NOTE-READ-REPO-1", "テスト太郎");
        secondReader = persistUser("TEST-NOTE-READ-REPO-2", "テスト花子");
        nonReader = persistUser("TEST-NOTE-READ-REPO-3", "テスト次郎");
        note = persistNote("既読対象の投稿です");
        otherNote = persistNote("別の投稿です");
    }

    private User persistUser(String employeeId, String name) {
        User user = new User();
        user.setEmployeeId(employeeId);
        user.setName(name);
        user.setPassword("dummy");
        user.setAdmin(false);
        user.setDeleted(false);
        return entityManager.persist(user);
    }

    private Note persistNote(String content) {
        Note newNote = new Note();
        newNote.setUser(firstReader);
        newNote.setCategory("業務連絡");
        newNote.setContent(content);
        newNote.setDeleted(false);
        return entityManager.persist(newNote);
    }

    private NoteRead persistNoteRead(User reader, Note targetNote) {
        NoteRead noteRead = new NoteRead();
        noteRead.setUser(reader);
        noteRead.setNote(targetNote);
        return entityManager.persist(noteRead);
    }

    // created_atはDBの既定値(トランザクション開始時刻)で登録され、同一テスト内では全件同じ値になるため、
    // 並び順を検証できるよう明示的な値に更新する
    private void updateCreatedAt(NoteRead noteRead, LocalDateTime createdAt) {
        entityManager.flush();
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE note_reads SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", noteRead.getId())
                .executeUpdate();
    }

    @Test
    void findByNote_IdOrderByCreatedAtAsc_returnsReadsOfNote_sortedByCreatedAtAsc() {
        NoteRead laterRead = persistNoteRead(firstReader, note);
        NoteRead earlierRead = persistNoteRead(secondReader, note);
        persistNoteRead(firstReader, otherNote);
        updateCreatedAt(laterRead, LocalDateTime.of(2000, 1, 2, 9, 0));
        updateCreatedAt(earlierRead, LocalDateTime.of(2000, 1, 1, 9, 0));
        entityManager.clear();

        List<NoteRead> actual = noteReadRepository.findByNote_IdOrderByCreatedAtAsc(note.getId());

        assertThat(actual).extracting(NoteRead::getId).containsExactly(earlierRead.getId(), laterRead.getId());
    }

    @Test
    void findByNote_IdOrderByCreatedAtAsc_fetchesUser() {
        persistNoteRead(secondReader, note);
        entityManager.flush();
        entityManager.clear();

        List<NoteRead> actual = noteReadRepository.findByNote_IdOrderByCreatedAtAsc(note.getId());

        assertThat(actual).hasSize(1);
        assertThat(Hibernate.isInitialized(actual.get(0).getUser())).isTrue();
        assertThat(actual.get(0).getUser().getName()).isEqualTo("テスト花子");
    }

    @Test
    void findNoteIdsByUserId_returnsNoteIdsReadByUser() {
        persistNoteRead(firstReader, note);
        persistNoteRead(firstReader, otherNote);
        persistNoteRead(secondReader, note);
        entityManager.flush();
        entityManager.clear();

        assertThat(noteReadRepository.findNoteIdsByUserId(firstReader.getId()))
                .containsExactlyInAnyOrder(note.getId(), otherNote.getId());
        assertThat(noteReadRepository.findNoteIdsByUserId(secondReader.getId())).containsExactly(note.getId());
    }

    @Test
    void findNoteIdsByUserId_returnsEmpty_whenUserHasNoReads() {
        persistNoteRead(firstReader, note);
        entityManager.flush();
        entityManager.clear();

        assertThat(noteReadRepository.findNoteIdsByUserId(nonReader.getId())).isEmpty();
    }

    @Test
    void countByNoteIds_countsReadsPerNote_andOmitsNotesWithoutReads() {
        Note unreadNote = persistNote("誰も既読にしていない投稿です");
        persistNoteRead(firstReader, note);
        persistNoteRead(secondReader, note);
        persistNoteRead(firstReader, otherNote);
        entityManager.flush();
        entityManager.clear();

        Map<Integer, Long> actual = noteReadRepository
                .countByNoteIds(List.of(note.getId(), otherNote.getId(), unreadNote.getId()))
                .stream()
                .collect(Collectors.toMap(NoteReadCount::getNoteId, NoteReadCount::getCount));

        // 既読者がいない投稿は集計結果に含まれない
        assertThat(actual).containsOnly(entry(note.getId(), 2L), entry(otherNote.getId(), 1L));
    }
}
