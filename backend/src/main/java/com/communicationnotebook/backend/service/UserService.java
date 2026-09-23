package com.communicationnotebook.backend.service;

import com.communicationnotebook.backend.dto.PasswordChangeRequest;
import com.communicationnotebook.backend.dto.PasswordResetResponse;
import com.communicationnotebook.backend.dto.SignupRequest;
import com.communicationnotebook.backend.dto.UserResponse;
import com.communicationnotebook.backend.dto.UserUpdateRequest;
import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import java.security.SecureRandom;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * ユーザー情報を取り扱うサービスです。
 * ユーザー情報の取得・更新・削除、サインアップ、パスワードのリセット・更新・仮パスワードの生成、管理者権限の確認、ユーザー状態の確認。
 */
@Service
public class UserService {

    private static final String TEMPORARY_PASSWORD_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final int TEMPORARY_PASSWORD_LENGTH = 12;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * ユーザー情報の全件取得
     * リポジトリから取得した未削除のユーザー情報を、UserResponse型のリストにして返却します。
     *
     * @param requesterId 実行者のID
     * @return ユーザー情報一覧（全件）
     * @throws ResponseStatusException 実行者が管理者権限を持たない場合（403 Forbidden）
     */
    public List<UserResponse> findAll(Integer requesterId) {
        requireAdmin(requesterId);
        return userRepository.findByDeletedFalse().stream()
                .map(UserResponse::from)
                .toList();
    }

    /**
     * ユーザー情報のID検索
     * IDに紐づくユーザー情報を取得します。
     *
     * @param requesterId 実行者のID
     * @param id ユーザーID
     * @return IDに紐づくユーザー情報
     * @throws ResponseStatusException 実行者が管理者権限を持たない場合（403 Forbidden）
     * @throws ResponseStatusException ユーザーが存在しない場合（404 Not Found）
     */
    public UserResponse findById(Integer requesterId, Integer id) {
        requireAdmin(requesterId);
        return UserResponse.from(findActiveUserOrThrow(id));
    }

    /**
     * ユーザー名の更新
     * IDに紐づくユーザー名を更新します。
     *
     * @param requesterId 実行者のID
     * @param targetUserId 更新対象ユーザーのID
     * @param request 新しいユーザー名
     * @return 更新されたユーザー情報
     * @throws ResponseStatusException 実行者が管理者権限を持たない場合（403 Forbidden）
     * @throws ResponseStatusException ユーザーが存在しない場合（404 Not Found）
     */
    public UserResponse updateName(Integer requesterId, Integer targetUserId, UserUpdateRequest request) {
        requireAdmin(requesterId);
        User target = findActiveUserOrThrow(targetUserId);
        target.setName(request.name());
        return UserResponse.from(userRepository.save(target));
    }

    /**
     * ユーザー情報の削除
     * IDに紐づくユーザー情報を論理削除（無効化）します。
     *
     * @param requesterId 実行者のID
     * @param targetUserId 削除対象ユーザーのID
     * @throws ResponseStatusException 実行者が管理者権限を持たない場合（403 Forbidden）
     * @throws ResponseStatusException ユーザーが存在しない場合（404 Not Found）
     */
    public void delete(Integer requesterId, Integer targetUserId) {
        requireAdmin(requesterId);
        User target = findActiveUserOrThrow(targetUserId);
        target.setDeleted(true);
        userRepository.save(target);
    }

    /**
     * セルフサインアップ
     * 職員ID・ユーザー名・パスワードを渡し、ユーザー情報を登録。
     *
     * @param request 職員ID・ユーザー名・パスワード
     * @return 登録されたユーザー情報
     * @throws ResponseStatusException 職員IDが既に使用されている場合（409 Conflict）
     */
    public User signup(SignupRequest request) {
        if (userRepository.existsByEmployeeId(request.employeeId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "職員IDは既に使用されています");
        }

        User user = new User();
        user.setEmployeeId(request.employeeId());
        user.setName(request.name());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setAdmin(false);
        user.setDeleted(false);
        return userRepository.save(user);
    }

    /**
     * パスワード変更
     * IDに紐づくユーザーのパスワードを変更します。
     * 新しいパスワードを暗号化してDBに保存します。
     *
     * @param userId ユーザーID
     * @param request 入力されたパスワード
     * @throws ResponseStatusException ユーザーが存在しない場合（404 Not Found）
     * @throws ResponseStatusException 現在のパスワードと入力されたパスワードが一致しない場合（401 Unauthorized）
     */
    public void changePassword(Integer userId, PasswordChangeRequest request) {
        User user = findActiveUserOrThrow(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "現在のパスワードが正しくありません");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /**
     * パスワードリセット
     * IDに紐づくユーザーのパスワードをリセットします。
     * 一時パスワードを生成し、暗号化してDBに保存します。
     *
     * @param requesterId 実行者のID
     * @param targetUserId パスワードリセット対象ユーザーのID
     * @return パスワードリセット後のユーザー名と仮パスワード
     * @throws ResponseStatusException 実行者が管理者権限を持たない場合（403 Forbidden）
     * @throws ResponseStatusException ユーザーが存在しない場合（404 Not Found）
     */
    public PasswordResetResponse resetPassword(Integer requesterId, Integer targetUserId) {
        requireAdmin(requesterId);
        User target = findActiveUserOrThrow(targetUserId);
        String temporaryPassword = generateTemporaryPassword();
        target.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(target);
        return new PasswordResetResponse(target.getName(), temporaryPassword);
    }

    //管理者権限有無のチェック
    private User requireAdmin(Integer requesterId) {
        User requester = findActiveUserOrThrow(requesterId);
        if (!requester.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理者のみ実行できます");
        }
        return requester;
    }

    //ユーザー状態の有効性チェック
    private User findActiveUserOrThrow(Integer userId) {
        return userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ユーザーが見つかりません"));
    }

    //一時パスワード生成。紛らわしい文字（0/O,1/l）は指定文字集合から除外しています。
    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (int i = 0; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            sb.append(TEMPORARY_PASSWORD_CHARS.charAt(secureRandom.nextInt(TEMPORARY_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
