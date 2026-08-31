package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.NoteReaderResponse;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.NoteRead;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.NoteReadRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class NoteReadService {

    private final NoteReadRepository noteReadRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public NoteReadService(
            NoteReadRepository noteReadRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.noteReadRepository = noteReadRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    public void register(Integer noteId, Integer userId) {
        Note note = noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + noteId));

        User user = userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        if (noteReadRepository.existsByUser_IdAndNote_Id(userId, noteId)) {
            return;
        }

        NoteRead noteRead = new NoteRead();
        noteRead.setUser(user);
        noteRead.setNote(note);
        noteReadRepository.save(noteRead);
    }

    public List<NoteReaderResponse> findReaders(Integer noteId) {
        noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + noteId));

        return noteReadRepository.findByNote_IdOrderByCreatedAtAsc(noteId).stream()
                .map(NoteReaderResponse::from)
                .toList();
    }
}
