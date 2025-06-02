package ru.practicum.compilation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCompilationDto {
    private Boolean pinned;
    @NotBlank(message = "Название подборки не может быть пустым")
    @Size(max = 50, message = "Название подборки не должно превышать 50 символов")
    private String title;
    private Set<Long> events;
}
