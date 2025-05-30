package ru.practicum.event.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.event.model.EventDto;
import ru.practicum.event.model.UpdateEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.request.model.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.EventRequestStatusUpdateResult;
import ru.practicum.request.model.ParticipationRequestDto;
import ru.practicum.request.service.ParticipationRequestService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/{userId}/events")
@Validated
public class SecuredEventController {
    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @GetMapping
    public ResponseEntity<List<EventDto>> getEventsByUserId(@PathVariable Long userId,
                                                            @RequestParam(defaultValue = "0") Integer from,
                                                            @RequestParam(defaultValue = "10") Integer size) {
        log.info("Получен запрос GET /users/{}/events", userId);
        return ResponseEntity.ok(eventService.findByUserId(userId, from, size));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventDto> getEventByUserId(@PathVariable Long userId,
                                                     @PathVariable Long eventId) {
        log.info("Получен запрос GET /users/{}/events/{}", userId, eventId);
        return ResponseEntity.ok(eventService.findByIdAndUser(userId, eventId));
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getRequests(@PathVariable Long userId,
                                                                     @PathVariable Long eventId) {
        log.info("Получен запрос GET /users/{}/events/{}/requests", userId, eventId);
        return ResponseEntity.ok(participationRequestService.getAllByEventAndInitiator(userId, eventId));
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public EventDto create(@PathVariable Long userId,
                           @RequestBody @Valid EventDto eventDto) {
        log.info("Получен запрос POST /users/{}/events c новым событием: {}", userId, eventDto);
        return eventService.create(userId, eventDto);
    }

    @PatchMapping("/{eventId}")
    public EventDto update(@PathVariable Long userId,
                           @PathVariable Long eventId,
                           @RequestBody @Valid UpdateEventDto eventDto) {
        log.info("Получен запрос PATCH /users/{}/events/{} c новым событием: {}", userId, eventId, eventDto);
        return eventService.updateByUser(userId, eventId, eventDto);

    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResult> updateRequest(@PathVariable Long userId,
                                                                        @PathVariable Long eventId,
                                                                        @RequestBody @Valid EventRequestStatusUpdateRequest eventDto) {
        log.info("Получен запрос PATCH /users/{}/events/{}/request с параметрами: {}", userId, eventId, eventDto);
        return ResponseEntity.ok(participationRequestService.updateStatus(userId, eventId, eventDto));
    }
}
