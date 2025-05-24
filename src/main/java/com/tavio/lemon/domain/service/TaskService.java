package com.tavio.lemon.domain.service;


import com.tavio.lemon.domain.repository.TaskRepository;
import com.tavio.lemon.entity.TaskEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class TaskService {
    private final TaskRepository repo;

    @Autowired
    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }

    public List<TaskEntity> getAllByUser(Long userId) {
        return repo.findByUserId(userId);
    }

    public Optional<TaskEntity> getById(Long id) {
        return repo.findById(id);
    }

    public TaskEntity save(TaskEntity task) {
        return repo.save(task);
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}