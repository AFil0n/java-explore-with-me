package ru.practicum.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.DateValidationException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.handler.model.ApiError;
import ru.practicum.utils.SimpleDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Objects;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(final NotFoundException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.NOT_FOUND.toString())
                .reason("Запрошенный объект не найден.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleConditionsNotMet(final ConditionsNotMetException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.CONFLICT.toString())
                .reason("Нарушены условия целостности данных.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleInvalidRequest(final MethodArgumentNotValidException e) {
        String field = Objects.requireNonNull(e.getBindingResult().getFieldError()).getField();
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Некорректный запрос.")
                .message("Некорректное значение параметра " + field + ": " + errorMessage)
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleСonstraintViolationException(final ConstraintViolationException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Нарушены условия валидации данных.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMessageNotReadableException(final HttpMessageNotReadableException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Некорректно составлен запрос.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleMissingRequestParameterException(final MissingServletRequestParameterException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Не передан обязательный параметр + " + e.getParameterName() + ".")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler(DateValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleDateValidationException(final DateValidationException e) {
        return ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.BAD_REQUEST.toString())
                .reason("Нарушены условия валидации дат.")
                .message(e.getMessage())
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGeneric(final Throwable e) {
        ApiError apiError = ApiError.builder()
                .errors(Arrays.stream(e.getStackTrace()).map(StackTraceElement::toString).toList())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.toString())
                .reason("Внутренняя ошибка сервера.")
                .timestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()))
                .build();

        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            apiError.setMessage(e.getMessage());
        } else {
            apiError.setMessage("Произошла непредвиденная ошибка.");
        }
        return apiError;
    }
}
