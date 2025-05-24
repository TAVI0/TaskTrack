package com.tavio.lemon.domain.repository;

import com.tavio.lemon.entity.TaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<TaskEntity, Long> {
    List<TaskEntity> findByUserId(Long userId);
}