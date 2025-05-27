package ru.practicum.handler;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private final String message;
    private List<String> errors;
}
