package com.communicationnotebook.backend.config;

import com.communicationnotebook.backend.entity.User;
import com.communicationnotebook.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * アプリ起動時に初期管理者を用意するためのクラスです。
 */
@Slf4j
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmployeeId;
    private final String adminName;
    private final String adminPassword;

    public AdminUserInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.employee-id}") String adminEmployeeId,
            @Value("${app.admin.name}") String adminName,
            @Value("${app.admin.password}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmployeeId = adminEmployeeId;
        this.adminName = adminName;
        this.adminPassword = adminPassword;
    }

    /**
     * アプリ起動時に初期管理者の作成処理を実行します。
     * 処理が失敗した場合、例外をキャッチしてエラーログを出力します。
     */
    @Override
    public void run(ApplicationArguments args) {
        try {
            initializeAdminUser();
        } catch (Exception e) {
            log.error("初期管理者ユーザーの自動作成に失敗しました", e);
        }
    }

    /**
     * 初期管理者を生成します。
     * 管理者が既に存在する場合と指定したIDのユーザーが管理者権限を持たない一般ユーザーとして存在している場合は、管理者は自動作成されません。
     */
    private void initializeAdminUser() {
        if (userRepository.existsByAdminTrueAndDeletedFalse()) {
            log.info("管理者ユーザーが既に存在するため、初期管理者の自動作成をスキップしました");
            return;
        }

        if (userRepository.findByEmployeeId(adminEmployeeId).isPresent()) {
            log.warn(
                    "職員ID '{}' のユーザーは既に存在しますが管理者ではありません。"
                            + "安全のため自動昇格は行いません。必要であれば手動で管理者権限を付与してください。",
                    adminEmployeeId);
            return;
        }

        User admin = new User();
        admin.setEmployeeId(adminEmployeeId);
        admin.setName(adminName);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setAdmin(true);
        admin.setDeleted(false);
        userRepository.save(admin);
        log.info("初期管理者ユーザーを作成しました: employeeId={}", adminEmployeeId);
    }
}
