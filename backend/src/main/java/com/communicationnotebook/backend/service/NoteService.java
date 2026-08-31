package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository;
import com.communicationnotebook.backend.repository.FavoriteRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;

    public NoteService(
            NoteRepository noteRepository,
            UserRepository userRepository,
            FavoriteRepository favoriteRepository,
            CommentRepository commentRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
    }

    public List<NoteResponse> findAll(String keyword, String category, boolean favoriteOnly, Integer userId) {
        String normalizedKeyword = normalize(keyword);
        String normalizedCategory = normalize(category);

        List<Note> notes = noteRepository.search(normalizedKeyword, normalizedCategory, favoriteOnly, userId);
        Set<Integer> favoriteNoteIds = favoriteRepository.findNoteIdsByUserId(userId);
        Map<Integer, Long> commentCounts = countCommentsByNoteId(notes.stream().map(Note::getId).toList());

        return notes.stream()
                .map(note -> NoteResponse.from(
                        note, favoriteNoteIds.contains(note.getId()), commentCounts.getOrDefault(note.getId(), 0L)))
                .toList();
    }

    private Map<Integer, Long> countCommentsByNoteId(List<Integer> noteIds) {
        Map<Integer, Long> counts = new HashMap<>();
        for (CommentRepository.NoteCommentCount count : commentRepository.countActiveByNoteIds(noteIds)) {
            counts.put(count.getNoteId(), count.getCount());
        }
        return counts;
    }

    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }

    public NoteResponse create(NoteCreateRequest request, Integer userId) {
        User user = userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        Note note = new Note();
        note.setUser(user);
        note.setCategory(request.category());
        note.setContent(request.content());
        note.setDeleted(false);

        Note saved = noteRepository.save(note);
        return NoteResponse.from(saved);
    }

    public NoteResponse update(Integer id, NoteUpdateRequest request, Integer userId) {
        Note note = noteRepository
                .findByIdWithUser(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + id));

        if (!note.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can update this note");
        }

        note.setCategory(request.category());
        note.setContent(request.content());
        note.setUpdatedAt(LocalDateTime.now());

        Note saved = noteRepository.save(note);
        long commentCount = countCommentsByNoteId(List.of(id)).getOrDefault(id, 0L);
        return NoteResponse.from(saved, false, commentCount);
    }

    public void delete(Integer id, Integer userId) {
        Note note = noteRepository
                .findByIdWithUser(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + id));

        User requester = userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        boolean isAuthor = note.getUser().getId().equals(requester.getId());
        if (!isAuthor && !requester.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author or an admin can delete this note");
        }

        note.setDeleted(true);
        noteRepository.save(note);
    }
}
