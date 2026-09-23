package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.NoteCreateRequest;
import com.communicationnotebook.backend.dto.NoteResponse;
import com.communicationnotebook.backend.dto.NoteUpdateRequest;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository;
import com.communicationnotebook.backend.repository.FavoriteRepository;
import com.communicationnotebook.backend.repository.NoteReadRepository;
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

/**
 * 投稿情報を取り扱うサービスです。
 * 投稿の検索・登録・更新・削除、投稿ごとのコメント数・既読者数取得を行います。
 */
@Service
public class NoteService {

    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final CommentRepository commentRepository;
    private final NoteReadRepository noteReadRepository;

    public NoteService(
            NoteRepository noteRepository,
            UserRepository userRepository,
            FavoriteRepository favoriteRepository,
            CommentRepository commentRepository,
            NoteReadRepository noteReadRepository) {
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.commentRepository = commentRepository;
        this.noteReadRepository = noteReadRepository;
    }

    /**
     * 投稿情報の取得を行います。
     * 
     * @param keyword 検索キーワード
     * @param category カテゴリ
     * @param favoriteOnly お気に入り登録済みのみ
     * @param userId ユーザーID
     * @return 検索条件に合致する投稿情報一覧。
     */
    public List<NoteResponse> findAll(String keyword, String category, boolean favoriteOnly, Integer userId) {
        String normalizedKeyword = normalize(keyword);
        String normalizedCategory = normalize(category);

        List<Note> notes = noteRepository.search(normalizedKeyword, normalizedCategory, favoriteOnly, userId);
        List<Integer> noteIds = notes.stream().map(Note::getId).toList();
        Set<Integer> favoriteNoteIds = favoriteRepository.findNoteIdsByUserId(userId);
        Set<Integer> readNoteIds = noteReadRepository.findNoteIdsByUserId(userId);
        Map<Integer, Long> commentCounts = countCommentsByNoteId(noteIds);
        Map<Integer, Long> readCounts = countReadsByNoteId(noteIds);

        return notes.stream()
                .map(note -> NoteResponse.from(
                        note,
                        favoriteNoteIds.contains(note.getId()),
                        commentCounts.getOrDefault(note.getId(), 0L),
                        readNoteIds.contains(note.getId()),
                        readCounts.getOrDefault(note.getId(), 0L)))
                .toList();
    }

    /**
     * 投稿情報の登録をします。
     * 
     * @param request 投稿の内容
     * @param userId ユーザーID
     * @return 登録済みの投稿情報
     * @throws ResponseStatusException ユーザーが存在しない、または削除済みの場合（404 Not Found）
     */
    public NoteResponse create(NoteCreateRequest request, Integer userId) {
        User user = userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        Note note = new Note();
        note.setUser(user);
        note.setCategory(request.category());
        note.setContent(request.content());
        note.setDeleted(false);

        Note saved = noteRepository.save(note);
        return NoteResponse.from(saved);
    }

    /**
     * 投稿情報の更新をします。
     * 
     * @param id 投稿ID
     * @param request 投稿内容
     * @param userId ユーザーID
     * @return 更新後の投稿情報
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException 実行者がユーザーIDと異なる場合（403 Forbidden）
     */
    public NoteResponse update(Integer id, NoteUpdateRequest request, Integer userId) {
        Note note = noteRepository
                .findByIdWithUser(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        if (!note.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "作成者のみ更新できます");
        }

        note.setCategory(request.category());
        note.setContent(request.content());
        note.setUpdatedAt(LocalDateTime.now());

        Note saved = noteRepository.save(note);
        long commentCount = countCommentsByNoteId(List.of(id)).getOrDefault(id, 0L);
        boolean read = noteReadRepository.existsByUser_IdAndNote_Id(userId, id);
        long readCount = countReadsByNoteId(List.of(id)).getOrDefault(id, 0L);
        return NoteResponse.from(saved, false, commentCount, read, readCount);
    }

    /**
     * 投稿情報を削除します。
     * 
     * @param id 投稿ID
     * @param userId ユーザーID
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException ユーザーが存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException 実行者が投稿者または管理者以外の場合（403 Forbidden）
     */
    public void delete(Integer id, Integer userId) {
        Note note = noteRepository
                .findByIdWithUser(id)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません"));

        User requester = userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));

        boolean isAuthor = note.getUser().getId().equals(requester.getId());
        if (!isAuthor && !requester.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "作成者または管理者のみ削除できます");
        }

        note.setDeleted(true);
        noteRepository.save(note);
    }

    /**
     * 投稿ごとのコメント数をカウントします。
     * 
     * @param noteIds 投稿ID一覧
     * @return 投稿ごとのコメント数
     */
    private Map<Integer, Long> countCommentsByNoteId(List<Integer> noteIds) {
        Map<Integer, Long> counts = new HashMap<>();
        for (CommentRepository.NoteCommentCount count : commentRepository.countActiveByNoteIds(noteIds)) {
            counts.put(count.getNoteId(), count.getCount());
        }
        return counts;
    }

    /**
     * 投稿ごとの既読者数をカウントします。
     * 
     * @param noteIds 投稿ID一覧
     * @return 投稿ごとの既読者数
     */
    private Map<Integer, Long> countReadsByNoteId(List<Integer> noteIds) {
        Map<Integer, Long> counts = new HashMap<>();
        for (NoteReadRepository.NoteReadCount count : noteReadRepository.countByNoteIds(noteIds)) {
            counts.put(count.getNoteId(), count.getCount());
        }
        return counts;
    }

    /**
     * 入力された値の正規化をします。
     * category・keywordともに、未送信時はnullが渡される想定ですが、
     * 空文字が渡された場合も絞込無効として扱えるよう防御的に正規化します。
     * 
     * @param value 入力された値
     * @return valueが空文字やスペースの場合null、それ以外の場合value
     */
    private String normalize(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}
