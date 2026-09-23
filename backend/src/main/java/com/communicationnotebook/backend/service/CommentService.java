package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.CommentCreateRequest;
import com.communicationnotebook.backend.dto.CommentResponse;
import com.communicationnotebook.backend.entity.Comment;
import com.communicationnotebook.backend.entity.Note;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.CommentRepository;
import com.communicationnotebook.backend.repository.NoteRepository;
import com.communicationnotebook.backend.repository.UserRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * コメント情報を取り扱うサービスです。
 * コメント情報の取得・登録・削除を行います。
 */
@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository, NoteRepository noteRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
    }

    /**
     * 投稿IDに紐づく削除されていないコメント情報を古い順に全件取得します。
     *
     * @param noteId 投稿ID
     * @return 未削除のコメント情報一覧（作成日時の古い順）
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     */
    public List<CommentResponse> findAll(Integer noteId) {
        findActiveNote(noteId);
        return commentRepository.findByNote_IdAndDeletedFalseOrderByCreatedAtAsc(noteId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    /**
     * 投稿に紐づくコメント情報を作成します。コメント投稿者の情報もあわせて登録します。
     *
     * @param noteId 投稿ID
     * @param request コメントの内容
     * @param userId コメント投稿者のユーザーID
     * @return 登録済みのコメント情報
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException ユーザーが存在しない、または削除済みの場合（404 Not Found）
     */
    public CommentResponse create(Integer noteId, CommentCreateRequest request, Integer userId) {
        Note note = findActiveNote(noteId);
        User user = findActiveUser(userId);

        Comment comment = new Comment();
        comment.setNote(note);
        comment.setUser(user);
        comment.setContent(request.content());
        comment.setDeleted(false);

        Comment saved = commentRepository.save(comment);
        return CommentResponse.from(saved);
    }

    /**
     * 投稿に紐づくコメント情報を削除します。
     *
     * @param noteId 投稿ID
     * @param commentId コメントID
     * @param userId 削除を実行するユーザーのID（コメント投稿者または管理者）
     * @throws ResponseStatusException コメントが存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException 削除実行者がコメント投稿者・管理者以外の場合（403 Forbidden）
     * @throws ResponseStatusException 投稿が存在しない、または削除済みの場合（404 Not Found）
     * @throws ResponseStatusException ユーザーが存在しない、または削除済みの場合（404 Not Found）
     */
    public void delete(Integer noteId, Integer commentId, Integer userId) {
        findActiveNote(noteId);
        User requester = findActiveUser(userId);

        Comment comment = commentRepository
                .findByIdAndNote_Id(commentId, noteId)
                .filter(c -> !c.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "コメントが見つかりません"));

        boolean isAuthor = comment.getUser().getId().equals(requester.getId());
        if (!isAuthor && !requester.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "作成者または管理者のみ削除できます");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    //投稿の有効性チェック
    private Note findActiveNote(Integer noteId) {
        return noteRepository
                .findById(noteId)
                .filter(n -> !n.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "投稿が見つかりません"));
    }

    //ユーザー状態の有効性チェック
    private User findActiveUser(Integer userId) {
        return userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    }
}
