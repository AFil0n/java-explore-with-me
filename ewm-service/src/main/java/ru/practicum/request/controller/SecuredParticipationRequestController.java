package ru.practicum.request.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.request.model.ParticipationRequestDto;
import ru.practicum.request.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/requests")
@Validated
public class SecuredParticipationRequestController {

    private final ParticipationRequestService participationRequestService;

    @GetMapping()
    public ResponseEntity<List<ParticipationRequestDto>> getAll(@PathVariable Long userId) {
        log.info("Получен запрос GET /users/{}/requests", userId);
        return ResponseEntity.ok(participationRequestService.getAllByUser(userId));
    }

    @PostMapping()
    public ResponseEntity<ParticipationRequestDto> create(@PathVariable Long userId,
                                                          @RequestParam Long eventId) {
        log.info("Получен запрос POST /users/{}/requests?eventId={}", userId, eventId);
        return new ResponseEntity<>(participationRequestService.create(userId, eventId), HttpStatus.CREATED);
    }

    @PatchMapping("/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancel(@PathVariable Long userId,
                                                          @PathVariable Long requestId) {
        log.info("Получен запрос PATCH /users/{}/requests/{}/cancel", userId, requestId);
        return ResponseEntity.ok(participationRequestService.cancel(userId, requestId));
    }
}