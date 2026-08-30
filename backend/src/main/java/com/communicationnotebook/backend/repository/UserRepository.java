package com.communicationnotebook.backend.repository;

import com.communicationnotebook.backend.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

    List<User> findByDeletedFalse();
}
