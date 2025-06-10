package com.bokszczanin.course.repository;

import com.bokszczanin.course.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartItem, UUID> {
    List<CartItem> findByStudentId(UUID studentId);
}