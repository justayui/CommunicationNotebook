package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteService(NoteRepository noteRepository, UserRepository userRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public List<NoteResponse> findAll() {
        return noteRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .map(NoteResponse::from)
                .toList();
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
        Note reloaded = noteRepository.findByIdWithUser(saved.getId()).orElseThrow();
        return NoteResponse.from(reloaded);
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
        return NoteResponse.from(saved);
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
