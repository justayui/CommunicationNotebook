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

/**
 * お気に入り情報を取り扱うサービスです。
 * お気に入り登録・解除を行います。
 */
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

    /**
     * 投稿IDに紐づく投稿のお気に入り登録をします。
     * 
     * @param noteId 投稿ID
     * @param userId ユーザーID
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException ユーザーが存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException すでにお気に入り登録済みの場合（409 Conflict）
     */
    public void register(Integer noteId, Integer userId) {
        Note note = noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        User user = userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        if (favoriteRepository.existsByUser_IdAndNote_Id(userId, noteId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "すでにお気に入りに登録されています");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setNote(note);
        favoriteRepository.save(favorite);
    }

    /**
     * 投稿IDに紐づく投稿のお気に入り登録を解除します。
     * 
     * @param noteId 投稿ID
     * @param userId ユーザーID
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException ユーザーが存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException お気に入りが見つからない場合（404 Not Found）
     */
    public void unregister(Integer noteId, Integer userId) {
        noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        Favorite favorite = favoriteRepository
                .findByUser_IdAndNote_Id(userId, noteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "お気に入りが見つかりません"));

        favoriteRepository.delete(favorite);
    }
}
