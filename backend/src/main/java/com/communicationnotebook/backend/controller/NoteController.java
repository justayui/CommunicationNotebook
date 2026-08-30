package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.service.NoteService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @GetMapping
    public List<NoteResponse> findAll() {
        return noteService.findAll();
    }
}
