package ru.practicum.request.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.request.model.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.EventRequestStatusUpdateResult;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequestMapper;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public List<ParticipationRequestDto> getAllByUser(Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        return participationRequestRepository.findAllByRequesterId(userId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto).toList();
    }

    public List<ParticipationRequestDto> getAllByEventAndInitiator(Long userId, Long eventId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявки на участие в событии может просмотреть только создатель события");
        }

        return participationRequestRepository.findAllByEventId(eventId)
                .stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .toList();
    }

    @Transactional
    public ParticipationRequestDto create(Long userId, Long eventId) {
        User requester = userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Нельзя заявить участие в собственном событии");
        }
        if (event.getState() != EventState.PUBLISHED) {
            throw new ConditionsNotMetException("Нельзя заявить участие в неопубликованном событии");
        }
        if (!participationRequestRepository.findAllByEventIdAndRequesterId(eventId, userId).isEmpty()) {
            throw new ConditionsNotMetException("Нельзя отправить дублирующую заявку на участие в событии");
        }
        if (event.getParticipantLimit() != 0 && Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
            throw new ConditionsNotMetException("Достигнут лимит заявок на участие в событии");
        }

        ParticipationRequest participationRequest = ParticipationRequest.builder()
                .requester(requester)
                .event(event)
                .status(event.getParticipantLimit() > 0 && event.getRequestModeration() ? ParticipationRequestStatus.PENDING : ParticipationRequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();
        if (!event.getRequestModeration()) {
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        }

        return ParticipationRequestMapper.toParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Transactional
    public ParticipationRequestDto cancel(Long userId, Long requestId) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Заявка с id=" + requestId + " не найдена"));
        if (!participationRequest.getRequester().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявку на участие в событии можно отменить только пользователем, который её отправил");
        }

        Event event = eventRepository.findById(participationRequest.getEvent().getId())
                .orElseThrow(() -> new NotFoundException("Событие с id=" + participationRequest.getEvent().getId() + " не найдено"));

        event.setConfirmedRequests(event.getConfirmedRequests() - 1);
        eventRepository.save(event);

        participationRequest.setStatus(ParticipationRequestStatus.CANCELED);

        participationRequestRepository.save(participationRequest);
        return ParticipationRequestMapper.toParticipationRequestDto(participationRequest);
    }

    @Transactional
    public EventRequestStatusUpdateResult updateStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest requestDto) {
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));
        if (!event.getInitiator().getId().equals(userId)) {
            throw new ConditionsNotMetException("Заявки на участие в событии может обновить только создатель события");
        }
        if (event.getParticipantLimit() == 0) {
            throw new ConditionsNotMetException("Нельзя обновить статус заявок на участие в событии с отключенной модерацией заявок");
        }

        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByEventId(eventId);
        List<Long> requestIds = participationRequests.stream().map(ParticipationRequest::getId).toList();
        List<Long> absentRequestIds = new ArrayList<>();
        requestDto.getRequestIds().forEach(id -> {
            if (!requestIds.contains(id)) {
                absentRequestIds.add(id);
            }
        });

        if (!absentRequestIds.isEmpty()) {
            throw new NotFoundException("Заявки на участие с id=" + absentRequestIds + " не найдены");
        }

        List<ParticipationRequest> participationRequestsToUpdate = participationRequests.stream()
                .filter(participationRequest -> requestDto.getRequestIds().contains(participationRequest.getId()))
                .toList();

        List<Long> notPendingRequests = participationRequestsToUpdate.stream()
                .filter(participationRequest -> participationRequest.getStatus() != ParticipationRequestStatus.PENDING)
                .map(ParticipationRequest::getId)
                .toList();

        if (!notPendingRequests.isEmpty()) {
            throw new ConditionsNotMetException("Заявки на участие в событии с id=" + eventId + " не находятся в состоянии ожидания подтверждения");
        }

        if (requestDto.getStatus() == ParticipationRequestStatus.CONFIRMED) {
            if (event.getConfirmedRequests() + requestDto.getRequestIds().size() > event.getParticipantLimit()) {
                throw new ConditionsNotMetException("Нельзя подтвердить заявки на участие в событии, так как превышен лимит заявок");
            }

            participationRequestsToUpdate.forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.CONFIRMED));
            participationRequestRepository.saveAll(participationRequests);
            event.setConfirmedRequests(event.getConfirmedRequests() + requestDto.getRequestIds().size());
            eventRepository.save(event);

            if (Objects.equals(event.getConfirmedRequests(), event.getParticipantLimit())) {
                List<ParticipationRequest> participationRequestsForDeny = participationRequests.stream()
                        .filter(participationRequest -> !requestDto.getRequestIds().contains(participationRequest.getId()))
                        .toList();

                participationRequestsForDeny
                        .forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.REJECTED));

                participationRequestRepository.saveAll(participationRequestsForDeny);
            }
        } else if (requestDto.getStatus() == ParticipationRequestStatus.REJECTED) {
            participationRequestsToUpdate.forEach(participationRequest -> participationRequest.setStatus(ParticipationRequestStatus.REJECTED));
            participationRequestRepository.saveAll(participationRequests);
            event.setConfirmedRequests(event.getConfirmedRequests() - requestDto.getRequestIds().size());
            eventRepository.save(event);
        }

        participationRequests = participationRequestRepository.findAllByEventId(eventId);
        return EventRequestStatusUpdateResult.builder()
                .confirmedRequests(participationRequests
                        .stream()
                        .filter(participationRequest -> participationRequest.getStatus() == ParticipationRequestStatus.CONFIRMED)
                        .map(ParticipationRequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toSet()))
                .rejectedRequests(participationRequests
                        .stream()
                        .filter(participationRequest -> participationRequest.getStatus() == ParticipationRequestStatus.REJECTED)
                        .map(ParticipationRequestMapper::toParticipationRequestDto)
                        .collect(Collectors.toSet()))
                .build();
    }
}
