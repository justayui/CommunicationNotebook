package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @InjectMocks
    private NoteService noteService;

    private Note newNote(Integer id, String category, String content, String authorName) {
        User user = new User();
        user.setName(authorName);

        Note note = new Note();
        note.setId(id);
        note.setUser(user);
        note.setCategory(category);
        note.setContent(content);
        note.setDeleted(false);
        note.setCreatedAt(LocalDateTime.of(2026, 8, 30, 10, 0));
        return note;
    }

    @Test
    void findAll_returnsNotesMappedToResponse() {
        Note note = newNote(1, "雑談", "これはテスト投稿です。", "テスト太郎");
        when(noteRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of(note));

        List<NoteResponse> result = noteService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("雑談");
        assertThat(result.get(0).content()).isEqualTo("これはテスト投稿です。");
        assertThat(result.get(0).author()).isEqualTo("テスト太郎");
    }

    @Test
    void findAll_returnsEmptyList_whenNoNotesExist() {
        when(noteRepository.findByDeletedFalseOrderByCreatedAtDesc()).thenReturn(List.of());

        List<NoteResponse> result = noteService.findAll();

        assertThat(result).isEmpty();
    }
}
