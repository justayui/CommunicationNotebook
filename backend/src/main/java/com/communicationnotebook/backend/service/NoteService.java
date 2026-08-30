package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
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

    public NoteResponse create(NoteCreateRequest request) {
        User user = userRepository
                .findById(request.userId())
                .filter(u -> !u.isDeleted())
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + request.userId()));

        Note note = new Note();
        note.setUser(user);
        note.setCategory(request.category());
        note.setContent(request.content());
        note.setDeleted(false);

        Note saved = noteRepository.save(note);
        Note reloaded = noteRepository.findByIdWithUser(saved.getId()).orElseThrow();
        return NoteResponse.from(reloaded);
    }
}
