package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.entity.Favorite;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.FavoriteRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FavoriteService favoriteService;

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
    void register_savesFavorite_whenNoteAndUserExistAndNotAlreadyFavorited() {
        Note note = newNote(10, false);
        User user = newUser(1, false);

        when(noteRepository.findById(10)).thenReturn(Optional.of(note));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(favoriteRepository.existsByUser_IdAndNote_Id(1, 10)).thenReturn(false);

        favoriteService.register(10, 1);

        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void register_throwsNotFound_whenNoteDoesNotExist() {
        when(noteRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.register(99, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void register_throwsNotFound_whenNoteIsDeleted() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, true)));

        assertThatThrownBy(() -> favoriteService.register(10, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void register_throwsNotFound_whenUserDoesNotExist() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> favoriteService.register(10, 99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void register_throwsNotFound_whenUserIsDeleted() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, true)));

        assertThatThrownBy(() -> favoriteService.register(10, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void register_throwsConflict_whenAlreadyFavorited() {
        when(noteRepository.findById(10)).thenReturn(Optional.of(newNote(10, false)));
        when(userRepository.findById(1)).thenReturn(Optional.of(newUser(1, false)));
        when(favoriteRepository.existsByUser_IdAndNote_Id(1, 10)).thenReturn(true);

        assertThatThrownBy(() -> favoriteService.register(10, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("already favorited");
    }
}
