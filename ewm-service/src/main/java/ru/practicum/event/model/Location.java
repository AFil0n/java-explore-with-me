package ru.practicum.event.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Location {
    @Positive(message = "Широта должна быть положительной")
    @NotNull(message = "Широта не может быть пустой")
    private Double lat;

    @Positive(message = "Долгота должна быть положительной")
    @NotNull(message = "Долгота не может быть пустой")
    private Double lon;
}
