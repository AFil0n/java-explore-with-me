package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.model.EventDto;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.UpdateEventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.request.model.EventRequestStatusUpdateRequest;
import ru.practicum.request.model.EventRequestStatusUpdateResult;
import ru.practicum.request.model.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.service.ParticipationRequestService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecuredEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private ParticipationRequestService participationRequestService;

    private EventDto eventShortDto;
    private EventDto eventFullDto;
    private EventDto newEventDto;
    private EventDto updateEventUserRequest;
    private ParticipationRequestDto participationRequestDto;
    private EventRequestStatusUpdateRequest statusUpdateRequest;
    private EventRequestStatusUpdateResult statusUpdateResult;

    @BeforeEach
    void setUp() {
        eventShortDto = new EventDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test Event Short");

        eventFullDto = new EventDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event Full");

        newEventDto = EventDto.builder()
                .annotation("Очень подробная аннотация предстоящего события, которая содержит все необходимые детали")
                .category(1L)
                .description("Полное описание события со всеми деталями и важной информацией для участников. " +
                        "Должно быть достаточно длинным, чтобы соответствовать требованиям валидации.")
                .eventDate(LocalDateTime.now().plusDays(2))
                .location(new Location(55.754167, 37.620000)) // Москва
                .paid(true)
                .participantLimit(100L)
                .requestModeration(true)
                .title("Интересное событие с длинным названием")
                .build();

        updateEventUserRequest = new EventDto();
        updateEventUserRequest.setTitle("Updated Title");

        participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(1L);
        participationRequestDto.setEvent(1L);
        participationRequestDto.setRequester(1L);

        statusUpdateRequest = new EventRequestStatusUpdateRequest();
        statusUpdateRequest.setRequestIds(Set.of(1L));
        statusUpdateRequest.setStatus(ParticipationRequestStatus.CONFIRMED);

        statusUpdateResult = new EventRequestStatusUpdateResult();
        statusUpdateResult.setConfirmedRequests(Set.of(participationRequestDto));
    }

    @Test
    void getEventsByUserIdShouldReturnEventShortDtoList() throws Exception {
        Mockito.when(eventService.findByUserId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/users/{userId}/events", 1L)
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventShortDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.getTitle()));

        Mockito.verify(eventService).findByUserId(1L, 0, 10);
    }

    @Test
    void getEventByUserIdShouldReturnEventFullDto() throws Exception {
        Mockito.when(eventService.findByIdAndUser(anyLong(), anyLong()))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/users/{userId}/events/{eventId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).findByIdAndUser(1L, 1L);
    }

    @Test
    void getRequestsShouldReturnParticipationRequestList() throws Exception {
        Mockito.when(participationRequestService.getAllByEventAndInitiator(anyLong(), anyLong()))
                .thenReturn(List.of(participationRequestDto));

        mockMvc.perform(get("/users/{userId}/events/{eventId}/requests", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(participationRequestDto.getId()));

        Mockito.verify(participationRequestService).getAllByEventAndInitiator(1L, 1L);
    }

    @Test
    void createShouldReturnCreatedEvent() throws Exception {
        Mockito.when(eventService.create(anyLong(), any(EventDto.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newEventDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).create(eq(1L), any(EventDto.class));
    }

    @Test
    void createWithInvalidDataShouldReturnBadRequest() throws Exception {
        EventDto invalidDto = new EventDto();

        mockMvc.perform(post("/users/{userId}/events", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateShouldReturnUpdatedEvent() throws Exception {
        Mockito.when(eventService.updateByUser(anyLong(), anyLong(), any(UpdateEventDto.class)))
                .thenReturn(eventFullDto);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateEventUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).updateByUser(eq(1L), eq(1L), any(UpdateEventDto.class));
    }

    @Test
    void updateWithInvalidDataShouldReturnBadRequest() throws Exception {
        EventDto invalidRequest = new EventDto();
        invalidRequest.setTitle("");

        mockMvc.perform(patch("/users/{userId}/events/{eventId}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateRequestShouldReturnStatusUpdateResult() throws Exception {
        Mockito.when(participationRequestService.updateStatus(anyLong(), anyLong(), any(EventRequestStatusUpdateRequest.class)))
                .thenReturn(statusUpdateResult);

        mockMvc.perform(patch("/users/{userId}/events/{eventId}/requests", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests[0].id").value(participationRequestDto.getId()));

        Mockito.verify(participationRequestService).updateStatus(eq(1L), eq(1L), any(EventRequestStatusUpdateRequest.class));
    }
}
