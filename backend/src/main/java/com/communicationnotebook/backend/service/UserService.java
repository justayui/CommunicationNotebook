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

    public List<UserResponse> findAll() {
        return userRepository.findByDeletedFalse().stream()
                .map(UserResponse::from)
                .toList();
    }

    public UserResponse findById(Integer id) {
        return UserResponse.from(findActiveUserOrThrow(id));
    }

    public UserResponse updateName(Integer requesterId, Integer targetUserId, UserUpdateRequest request) {
        requireAdmin(requesterId);
        User target = findActiveUserOrThrow(targetUserId);
        target.setName(request.name());
        return UserResponse.from(userRepository.save(target));
    }

    public void delete(Integer requesterId, Integer targetUserId) {
        requireAdmin(requesterId);
        User target = findActiveUserOrThrow(targetUserId);
        target.setDeleted(true);
        userRepository.save(target);
    }

    public PasswordResetResponse resetPassword(Integer requesterId, Integer targetUserId) {
        requireAdmin(requesterId);
        User target = findActiveUserOrThrow(targetUserId);
        String temporaryPassword = generateTemporaryPassword();
        target.setPassword(passwordEncoder.encode(temporaryPassword));
        userRepository.save(target);
        return new PasswordResetResponse(target.getName(), temporaryPassword);
    }

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

    public void changePassword(Integer userId, PasswordChangeRequest request) {
        User user = findActiveUserOrThrow(userId);

        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "現在のパスワードが正しくありません");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    private User requireAdmin(Integer requesterId) {
        User requester = findActiveUserOrThrow(requesterId);
        if (!requester.isAdmin()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "管理者のみ実行できます");
        }
        return requester;
    }

    private User findActiveUserOrThrow(Integer userId) {
        return userRepository
                .findById(userId)
                .filter(u -> !u.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userId));
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(TEMPORARY_PASSWORD_LENGTH);
        for (int i = 0; i < TEMPORARY_PASSWORD_LENGTH; i++) {
            sb.append(TEMPORARY_PASSWORD_CHARS.charAt(secureRandom.nextInt(TEMPORARY_PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }
}
