package com.communicationnotebook.backend.controller;

import com.communicationnotebook.backend.security.UserPrincipal;
import com.communicationnotebook.backend.service.FavoriteService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notes/{noteId}/favorites")
public class FavoriteController {

    private final FavoriteService favoriteService;

    public FavoriteController(FavoriteService favoriteService) {
        this.favoriteService = favoriteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@PathVariable Integer noteId, @AuthenticationPrincipal UserPrincipal principal) {
        favoriteService.register(noteId, principal.getId());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable Integer noteId, @AuthenticationPrincipal UserPrincipal principal) {
        favoriteService.unregister(noteId, principal.getId());
    }
}
