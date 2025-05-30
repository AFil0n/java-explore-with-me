package ru.practicum.compilation.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.event.model.Event;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompilationDto {
    private Long id;
    @NotBlank(message = "Название подборки не может быть пустым")
    @Size(max = 50, message = "Название подборки не должно превышать 50 символов")
    private String title;
    private Boolean pinned;
    private List<Event> events;
}
