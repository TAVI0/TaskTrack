package com.tavio.lemon.web.mapper;

import com.tavio.lemon.entity.TaskEntity;
import com.tavio.lemon.entity.UserEntity;
import com.tavio.lemon.web.dto.TaskDto;

public class TaskMapper {
    public static TaskDto toDto(TaskEntity e) {
        return TaskDto.builder()
                .id(e.getId())
                .title(e.getTitle())
                .description(e.getDescription())
                .completed(e.isCompleted())
                .createdAt(e.getCreatedAt())
                .dueDate(e.getDueDate())
                .userId(e.getUser().getId())
                .build();
    }

    public static TaskEntity toEntity(TaskDto dto, UserEntity user) {
        TaskEntity.TaskEntityBuilder builder = TaskEntity.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .completed(dto.isCompleted())
                .dueDate(dto.getDueDate())
                .user(user);

        if (dto.getId() != null) {
            builder.id(dto.getId());
        }

        return builder.build();
    }
}