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
 * V5マイグレーションはE001/E002が既に存在する前提のUPDATE文のみでINSERTを行わないため、
 * まっさらなDBでは管理者が作成されない。その代わりにアプリ起動時に初期管理者を用意する。
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

    @Override
    public void run(ApplicationArguments args) {
        try {
            initializeAdminUser();
        } catch (Exception e) {
            log.error("初期管理者ユーザーの自動作成に失敗しました", e);
        }
    }

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
