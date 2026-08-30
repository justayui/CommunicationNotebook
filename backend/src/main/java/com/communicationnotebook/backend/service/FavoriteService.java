package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.entity.Favorite;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.FavoriteRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public FavoriteService(
            FavoriteRepository favoriteRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.favoriteRepository = favoriteRepository;
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

        if (favoriteRepository.existsByUser_IdAndNote_Id(userId, noteId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Note is already favorited: " + noteId);
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setNote(note);
        favoriteRepository.save(favorite);
    }

    public void unregister(Integer noteId, Integer userId) {
        noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found: " + noteId));

        userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));

        Favorite favorite = favoriteRepository
                .findByUser_IdAndNote_Id(userId, noteId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Favorite not found: note " + noteId));

        favoriteRepository.delete(favorite);
    }
}
