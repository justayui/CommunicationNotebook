package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.NoteReaderResponse;
import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.NoteReadService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes/{noteId}/reads")
public class NoteReadController {

    private final NoteReadService noteReadService;

    public NoteReadController(NoteReadService noteReadService) {
        this.noteReadService = noteReadService;
    }

    @GetMapping
    public List<NoteReaderResponse> findReaders(@PathVariable Integer noteId) {
        return noteReadService.findReaders(noteId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@PathVariable Integer noteId, @AuthenticationPrincipal UserPrincipal principal) {
        noteReadService.register(noteId, principal.getId());
    }
}
