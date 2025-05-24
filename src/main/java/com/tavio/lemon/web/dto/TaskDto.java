package com.tavio.lemon.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;
    private String title;
    private String description;
    @Schema(description = "Marca si la tarea está completada", defaultValue = "false")
    private boolean completed;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime createdAt;
    private LocalDateTime dueDate;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Long userId;
}