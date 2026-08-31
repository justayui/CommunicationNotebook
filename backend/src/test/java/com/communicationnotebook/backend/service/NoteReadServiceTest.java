package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.NoteReaderResponse;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.NoteRead;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteReadRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class NoteReadServiceTest {

    @Mock
    private NoteReadRepository noteReadRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NoteReadService noteReadService;

    private Note newNote(Integer id, boolean deleted) {
        Note note = new Note();
        note.setId(id);
        note.setDeleted(deleted);
        return note;
    }

    private User newUser(Integer id, boolean deleted) {
        User user = new User();
        user.setId(id);
        user.setDeleted(deleted);
        return user;
    }

    @Test
    void register_savesNoteRead_whenNoteAndUserExistAndNotAlreadyRead() {
        Note note = newNote(10, false);
        User user = newUser(1, false);

        when(noteRepository.findById(10)).thenReturn(Optional.of(note));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(noteReadRepository.existsByUser_IdAndNote_Id(1, 10)).thenReturn(false);

        noteReadService.register(10, 1);

        verify(noteReadRepository).save(any(NoteRead.class));
    }

    @Test
    void register_throwsNotFound_whenNoteDoesNotExist() {
        when(noteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteReadService.register(99, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void register_throwsNotFound_whenNoteIsDeleted() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, true)));

        assertThatThrownBy(() -> noteReadService.register(10, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void register_throwsNotFound_whenUserDoesNotExist() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteReadService.register(10, 99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void register_throwsNotFound_whenUserIsDeleted() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, true)));

        assertThatThrownBy(() -> noteReadService.register(10, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void register_doesNothing_whenAlreadyRead() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, false)));
        when(noteReadRepository.existsByUser_IdAndNote_Id(1, 10)).thenReturn(true);

        noteReadService.register(10, 1);

        verify(noteReadRepository, never()).save(any(NoteRead.class));
    }

    @Test
    void findReaders_returnsReadersForNote() {
        User user = newUser(1, false);
        user.setName("テスト太郎");
        NoteRead noteRead = new NoteRead();
        noteRead.setUser(user);

        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(noteReadRepository.findByNote_IdOrderByCreatedAtAsc(10)).thenReturn(List.of(noteRead));

        List<NoteReaderResponse> result = noteReadService.findReaders(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).userId()).isEqualTo(1);
        assertThat(result.get(0).name()).isEqualTo("テスト太郎");
    }

    @Test
    void findReaders_throwsNotFound_whenNoteDoesNotExist() {
        when(noteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteReadService.findReaders(99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void findReaders_throwsNotFound_whenNoteIsDeleted() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, true)));

        assertThatThrownBy(() -> noteReadService.findReaders(10))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }
}
