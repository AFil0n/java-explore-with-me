package ru.practicum.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.request.model.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.EventRequestStatusUpdateResult;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParticipationRequestServiceTest {

    @Mock
    private ParticipationRequestRepository participationRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private ParticipationRequestService participationRequestService;

    private User user;
    private User initiator;
    private Event event;
    private ParticipationRequest request;
    private EventRequestStatusUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("User");
        user.setEmail("user@example.com");

        initiator = new User();
        initiator.setId(2L);
        initiator.setName("Initiator");
        initiator.setEmail("initiator@example.com");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setInitiator(initiator);
        event.setState(EventState.PUBLISHED);
        event.setParticipantLimit(10L);
        event.setConfirmedRequests(5L);
        event.setRequestModeration(true);

        request = new ParticipationRequest();
        request.setId(1L);
        request.setRequester(user);
        request.setEvent(event);
        request.setStatus(ParticipationRequestStatus.PENDING);
        request.setCreated(LocalDateTime.now());

        updateRequest = new EventRequestStatusUpdateRequest();
        updateRequest.setRequestIds(Set.of(1L));
        updateRequest.setStatus(ParticipationRequestStatus.CONFIRMED);
    }

    @Test
    void getAllByUserShouldReturnRequests() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(participationRequestRepository.findAllByRequesterId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = participationRequestService.getAllByUser(1L);

        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
        verify(userRepository).findById(1L);
        verify(participationRequestRepository).findAllByRequesterId(1L);
    }

    @Test
    void getAllByUserWhenUserNotFoundShouldThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> participationRequestService.getAllByUser(1L));
        verify(userRepository).findById(1L);
        verify(participationRequestRepository, never()).findAllByRequesterId(any());
    }

    @Test
    void getAllByEventAndInitiatorShouldReturnRequests() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        List<ParticipationRequestDto> result = participationRequestService.getAllByEventAndInitiator(2L, 1L);

        assertEquals(1, result.size());
        assertEquals(request.getId(), result.get(0).getId());
        verify(userRepository).findById(2L);
        verify(eventRepository).findById(1L);
        verify(participationRequestRepository).findAllByEventId(1L);
    }

    @Test
    void getAllByEventAndInitiatorWhenNotInitiatorShouldThrowConditionsNotMetException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.getAllByEventAndInitiator(1L, 1L));
    }

    @Test
    void createShouldCreateRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventIdAndRequesterId(1L, 1L)).thenReturn(List.of());
        when(participationRequestRepository.save(any())).thenReturn(request);

        ParticipationRequestDto result = participationRequestService.create(1L, 1L);

        assertNotNull(result);
        assertEquals(request.getId(), result.getId());
        verify(userRepository).findById(1L);
        verify(eventRepository).findById(1L);
        verify(participationRequestRepository).save(any());
    }

    @Test
    void createWhenOwnEventShouldThrowConditionsNotMetException() {
        event.setInitiator(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.create(1L, 1L));
    }

    @Test
    void createWhenEventNotPublishedShouldThrowConditionsNotMetException() {
        event.setState(EventState.PENDING);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.create(1L, 1L));
    }

    @Test
    void createWhenDuplicateRequestShouldThrowConditionsNotMetException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventIdAndRequesterId(1L, 1L)).thenReturn(List.of(request));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.create(1L, 1L));
    }

    @Test
    void createWhenLimitReachedShouldThrowConditionsNotMetException() {
        event.setConfirmedRequests(10L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventIdAndRequesterId(1L, 1L)).thenReturn(List.of());

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.create(1L, 1L));
    }

    @Test
    void cancelShouldCancelRequest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(participationRequestRepository.findById(1L)).thenReturn(Optional.of(request));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.save(any())).thenReturn(request);

        ParticipationRequestDto result = participationRequestService.cancel(1L, 1L);

        assertEquals(ParticipationRequestStatus.CANCELED, request.getStatus());
        assertEquals(4L, event.getConfirmedRequests());
        assertNotNull(result);
    }

    @Test
    void cancelWhenNotRequesterShouldThrowConditionsNotMetException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(participationRequestRepository.findById(1L)).thenReturn(Optional.of(request));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.cancel(2L, 1L));
    }

    @Test
    void updateStatusShouldConfirmRequests() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        EventRequestStatusUpdateResult result = participationRequestService.updateStatus(2L, 1L, updateRequest);

        assertEquals(1, result.getConfirmedRequests().size());
        assertEquals(0, result.getRejectedRequests().size());
        assertEquals(6L, event.getConfirmedRequests());
        assertEquals(ParticipationRequestStatus.CONFIRMED, request.getStatus());
    }

    @Test
    void updateStatusWhenNotInitiatorShouldThrowConditionsNotMetException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.updateStatus(1L, 1L, updateRequest));
    }

    @Test
    void updateStatusWhenNoModerationShouldThrowConditionsNotMetException() {
        event.setRequestModeration(false);
        event.setParticipantLimit(0L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> participationRequestService.updateStatus(2L, 1L, updateRequest)
        );

        assertEquals("Нельзя обновить статус заявок на участие в событии с отключенной модерацией заявок",
                exception.getMessage());
    }

    @Test
    void updateStatusWhenLimitReachedShouldRejectOtherRequests() {
        event.setConfirmedRequests(9L);
        event.setParticipantLimit(10L);

        ParticipationRequest request2 = new ParticipationRequest();
        request2.setId(2L);
        request2.setRequester(user);
        request2.setEvent(event);
        request2.setStatus(ParticipationRequestStatus.PENDING);
        request2.setCreated(LocalDateTime.now());

        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L))
                .thenReturn(List.of(request, request2));

        updateRequest.setRequestIds(Set.of(1L));

        EventRequestStatusUpdateResult result = participationRequestService.updateStatus(2L, 1L, updateRequest);

        assertEquals(1, result.getConfirmedRequests().size());
        assertEquals(1, result.getRejectedRequests().size());
        assertEquals(ParticipationRequestStatus.CONFIRMED, request.getStatus());
        assertEquals(ParticipationRequestStatus.REJECTED, request2.getStatus());
        assertEquals(10L, event.getConfirmedRequests());
    }

    @Test
    void updateStatusWhenParticipantLimitZeroShouldThrowException() {
        event.setParticipantLimit(0L);
        event.setRequestModeration(true);

        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> participationRequestService.updateStatus(2L, 1L, updateRequest)
        );

        assertEquals("Нельзя обновить статус заявок на участие в событии с отключенной модерацией заявок",
                exception.getMessage());
    }

    @Test
    void updateStatusWhenRequestNotFoundShouldThrowNotFoundException() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(List.of());

        assertThrows(NotFoundException.class,
                () -> participationRequestService.updateStatus(2L, 1L, updateRequest));
    }

    @Test
    void updateStatusWhenNotPendingShouldThrowConditionsNotMetException() {
        request.setStatus(ParticipationRequestStatus.CONFIRMED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.updateStatus(2L, 1L, updateRequest));
    }

    @Test
    void updateStatusWhenLimitExceededShouldThrowConditionsNotMetException() {
        event.setConfirmedRequests(10L);
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        assertThrows(ConditionsNotMetException.class,
                () -> participationRequestService.updateStatus(2L, 1L, updateRequest));
    }

    @Test
    void updateStatusWhenRejectShouldUpdateStatus() {
        updateRequest.setStatus(ParticipationRequestStatus.REJECTED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(initiator));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(participationRequestRepository.findAllByEventId(1L)).thenReturn(List.of(request));

        EventRequestStatusUpdateResult result = participationRequestService.updateStatus(2L, 1L, updateRequest);

        assertEquals(0, result.getConfirmedRequests().size());
        assertEquals(1, result.getRejectedRequests().size());
        assertEquals(ParticipationRequestStatus.REJECTED, request.getStatus());
    }
}
