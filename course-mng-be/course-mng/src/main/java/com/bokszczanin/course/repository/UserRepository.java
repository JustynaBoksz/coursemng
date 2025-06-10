package com.bokszczanin.course.repository;

import com.bokszczanin.course.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserData, UUID> {
    Optional<UserData> findByUsername(String username);
}