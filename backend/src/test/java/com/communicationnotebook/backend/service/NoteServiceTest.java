package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NoteServiceTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

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

    @Test
    void create_savesNoteAndReturnsResponse() {
        User user = new User();
        user.setId(1);
        user.setName("テスト太郎");
        user.setDeleted(false);

        NoteCreateRequest request = new NoteCreateRequest(1, "雑談", "これはテスト投稿です。");

        Note saved = new Note();
        saved.setId(10);

        Note reloaded = newNote(10, "雑談", "これはテスト投稿です。", "テスト太郎");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenReturn(saved);
        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(reloaded));

        NoteResponse result = noteService.create(request);

        assertThat(result.id()).isEqualTo(10);
        assertThat(result.category()).isEqualTo("雑談");
        assertThat(result.content()).isEqualTo("これはテスト投稿です。");
        assertThat(result.author()).isEqualTo("テスト太郎");
    }

    @Test
    void create_throwsNotFound_whenUserDoesNotExist() {
        NoteCreateRequest request = new NoteCreateRequest(99, "雑談", "これはテスト投稿です。");
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void create_throwsNotFound_whenUserIsDeleted() {
        User deletedUser = new User();
        deletedUser.setId(2);
        deletedUser.setDeleted(true);

        NoteCreateRequest request = new NoteCreateRequest(2, "雑談", "これはテスト投稿です。");
        when(userRepository.findById(2)).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> noteService.create(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void update_updatesNoteAndReturnsResponse() {
        User user = new User();
        user.setId(1);
        user.setName("テスト太郎");

        Note note = new Note();
        note.setId(10);
        note.setUser(user);
        note.setCategory("雑談");
        note.setContent("元の内容");
        note.setDeleted(false);
        note.setCreatedAt(LocalDateTime.of(2026, 8, 30, 10, 0));

        NoteUpdateRequest request = new NoteUpdateRequest(1, "業務連絡", "更新後の内容");

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));

        NoteResponse result = noteService.update(10, request);

        assertThat(result.category()).isEqualTo("業務連絡");
        assertThat(result.content()).isEqualTo("更新後の内容");
        assertThat(result.author()).isEqualTo("テスト太郎");
    }

    @Test
    void update_throwsNotFound_whenNoteDoesNotExist() {
        NoteUpdateRequest request = new NoteUpdateRequest(1, "雑談", "更新後の内容");
        when(noteRepository.findByIdWithUser(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.update(99, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void update_throwsNotFound_whenNoteIsDeleted() {
        User user = new User();
        user.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(user);
        note.setDeleted(true);

        NoteUpdateRequest request = new NoteUpdateRequest(1, "雑談", "更新後の内容");
        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.update(10, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void update_throwsForbidden_whenRequesterIsNotAuthor() {
        User user = new User();
        user.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(user);
        note.setDeleted(false);

        NoteUpdateRequest request = new NoteUpdateRequest(2, "雑談", "更新後の内容");
        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.update(10, request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the author");
    }
}
