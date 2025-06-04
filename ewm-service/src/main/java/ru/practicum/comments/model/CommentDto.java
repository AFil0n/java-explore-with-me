package ru.practicum.comments.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.model.EventDto;
import ru.practicum.user.model.UserDto;

import java.time.LocalDateTime;

import static ru.practicum.utils.SimpleDateTimeFormatter.PATTERN;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CommentDto {
    Long id;
    UserDto user;
    EventDto event;
    String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = PATTERN)
    LocalDateTime created;
}
