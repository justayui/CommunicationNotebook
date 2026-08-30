package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.repository.NoteRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NoteService {

    private final NoteRepository noteRepository;

    public NoteService(NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
    }

    public List<NoteResponse> findAll() {
        return noteRepository.findByDeletedFalseOrderByCreatedAtDesc().stream()
                .map(NoteResponse::from)
                .toList();
    }
}
