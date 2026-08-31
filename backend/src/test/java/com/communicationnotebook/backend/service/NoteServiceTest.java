package com.communicationnotebook.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository;
import com.communicationnotebook.backend.repository.FavoriteRepository;
import com.communicationnotebook.backend.repository.NoteReadRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NoteReadRepository noteReadRepository;

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
        when(noteRepository.search(null, null, false, 1)).thenReturn(List.of(note));
        when(favoriteRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of());

        List<NoteResponse> result = noteService.findAll(null, null, false, 1);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).category()).isEqualTo("雑談");
        assertThat(result.get(0).content()).isEqualTo("これはテスト投稿です。");
        assertThat(result.get(0).author()).isEqualTo("テスト太郎");
        assertThat(result.get(0).favorited()).isFalse();
        assertThat(result.get(0).commentCount()).isZero();
    }

    @Test
    void findAll_returnsEmptyList_whenNoNotesExist() {
        when(noteRepository.search(null, null, false, 1)).thenReturn(List.of());
        when(favoriteRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of());

        List<NoteResponse> result = noteService.findAll(null, null, false, 1);

        assertThat(result).isEmpty();
    }

    @Test
    void findAll_normalizesBlankKeywordAndCategoryToNull() {
        when(noteRepository.search(null, null, false, 1)).thenReturn(List.of());
        when(favoriteRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of());

        noteService.findAll("  ", "", false, 1);

        verify(noteRepository).search(null, null, false, 1);
    }

    @Test
    void findAll_passesKeywordCategoryFavoriteOnlyToRepository() {
        when(noteRepository.search("懇親会", "業務連絡", true, 1)).thenReturn(List.of());
        when(favoriteRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of());

        noteService.findAll("懇親会", "業務連絡", true, 1);

        verify(noteRepository).search("懇親会", "業務連絡", true, 1);
    }

    @Test
    void findAll_marksFavoritedTrue_whenNoteIdInFavoriteSet() {
        Note note = newNote(1, "雑談", "これはテスト投稿です。", "テスト太郎");
        when(noteRepository.search(null, null, false, 1)).thenReturn(List.of(note));
        when(favoriteRepository.findNoteIdsByUserId(1)).thenReturn(Set.of(1));
        when(noteReadRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of());

        List<NoteResponse> result = noteService.findAll(null, null, false, 1);

        assertThat(result.get(0).favorited()).isTrue();
    }

    @Test
    void findAll_includesCommentCount_whenCommentsExistForNote() {
        Note note = newNote(1, "雑談", "これはテスト投稿です。", "テスト太郎");
        when(noteRepository.search(null, null, false, 1)).thenReturn(List.of(note));
        when(favoriteRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.findNoteIdsByUserId(1)).thenReturn(Set.of());
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());
        CommentRepository.NoteCommentCount count = new CommentRepository.NoteCommentCount() {
            @Override
            public Integer getNoteId() {
                return 1;
            }

            @Override
            public Long getCount() {
                return 3L;
            }
        };
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of(count));

        List<NoteResponse> result = noteService.findAll(null, null, false, 1);

        assertThat(result.get(0).commentCount()).isEqualTo(3L);
    }

    @Test
    void create_savesNoteAndReturnsResponse() {
        User user = new User();
        user.setId(1);
        user.setName("テスト太郎");
        user.setDeleted(false);

        NoteCreateRequest request = new NoteCreateRequest("雑談", "これはテスト投稿です。");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> {
            Note note = invocation.getArgument(0);
            note.setId(10);
            return note;
        });

        NoteResponse result = noteService.create(request, 1);

        assertThat(result.id()).isEqualTo(10);
        assertThat(result.category()).isEqualTo("雑談");
        assertThat(result.content()).isEqualTo("これはテスト投稿です。");
        assertThat(result.author()).isEqualTo("テスト太郎");
        verify(noteRepository, never()).findByIdWithUser(any());
    }

    @Test
    void create_throwsNotFound_whenUserDoesNotExist() {
        NoteCreateRequest request = new NoteCreateRequest("雑談", "これはテスト投稿です。");
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.create(request, 99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void create_throwsNotFound_whenUserIsDeleted() {
        User deletedUser = new User();
        deletedUser.setId(2);
        deletedUser.setDeleted(true);

        NoteCreateRequest request = new NoteCreateRequest("雑談", "これはテスト投稿です。");
        when(userRepository.findById(2)).thenReturn(Optional.of(deletedUser));

        assertThatThrownBy(() -> noteService.create(request, 2))
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

        NoteUpdateRequest request = new NoteUpdateRequest("業務連絡", "更新後の内容");

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));
        when(noteRepository.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.countActiveByNoteIds(any())).thenReturn(List.of());
        when(noteReadRepository.existsByUser_IdAndNote_Id(1, 10)).thenReturn(false);
        when(noteReadRepository.countByNoteIds(any())).thenReturn(List.of());

        NoteResponse result = noteService.update(10, request, 1);

        assertThat(result.category()).isEqualTo("業務連絡");
        assertThat(result.content()).isEqualTo("更新後の内容");
        assertThat(result.author()).isEqualTo("テスト太郎");
    }

    @Test
    void update_throwsNotFound_whenNoteDoesNotExist() {
        NoteUpdateRequest request = new NoteUpdateRequest("雑談", "更新後の内容");
        when(noteRepository.findByIdWithUser(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.update(99, request, 1))
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

        NoteUpdateRequest request = new NoteUpdateRequest("雑談", "更新後の内容");
        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.update(10, request, 1))
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

        NoteUpdateRequest request = new NoteUpdateRequest("雑談", "更新後の内容");
        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.update(10, request, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the author");
    }

    @Test
    void delete_marksNoteAsDeleted_whenRequesterIsAuthor() {
        User author = new User();
        author.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(author);
        note.setDeleted(false);

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));
        when(userRepository.findById(1)).thenReturn(Optional.of(author));

        noteService.delete(10, 1);

        assertThat(note.isDeleted()).isTrue();
    }

    @Test
    void delete_marksNoteAsDeleted_whenRequesterIsAdmin() {
        User author = new User();
        author.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(author);
        note.setDeleted(false);

        User admin = new User();
        admin.setId(2);
        admin.setAdmin(true);

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));
        when(userRepository.findById(2)).thenReturn(Optional.of(admin));

        noteService.delete(10, 2);

        assertThat(note.isDeleted()).isTrue();
    }

    @Test
    void delete_throwsNotFound_whenNoteDoesNotExist() {
        when(noteRepository.findByIdWithUser(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.delete(99, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void delete_throwsNotFound_whenNoteIsAlreadyDeleted() {
        User author = new User();
        author.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(author);
        note.setDeleted(true);

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));

        assertThatThrownBy(() -> noteService.delete(10, 1))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Note not found");
    }

    @Test
    void delete_throwsNotFound_whenUserDoesNotExist() {
        User author = new User();
        author.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(author);
        note.setDeleted(false);

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> noteService.delete(10, 99))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void delete_throwsForbidden_whenRequesterIsNeitherAuthorNorAdmin() {
        User author = new User();
        author.setId(1);

        Note note = new Note();
        note.setId(10);
        note.setUser(author);
        note.setDeleted(false);

        User otherUser = new User();
        otherUser.setId(2);
        otherUser.setAdmin(false);

        when(noteRepository.findByIdWithUser(10)).thenReturn(Optional.of(note));
        when(userRepository.findById(2)).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> noteService.delete(10, 2))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Only the author or an admin");
    }
}
