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
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventDto eventDto;
    private EventDto updateRequest;

    @BeforeEach
    void setUp() {
        eventDto = new EventDto();
        eventDto.setId(1L);
        eventDto.setTitle("Test Event");
        eventDto.setAnnotation("Test Annotation");
        eventDto.setEventDate(LocalDateTime.now().plusDays(1));

        updateRequest = new EventDto();
        updateRequest.setTitle("Updated Title");
    }

    @Test
    void getAllEventsWithAllParamsShouldReturnOk() throws Exception {
        Mockito.when(eventService.searchAdmin(any()))
                .thenReturn(List.of(eventDto));

        mockMvc.perform(get("/admin/events")
                        .param("users", "1,2")
                        .param("states", "PENDING,PUBLISHED")
                        .param("categories", "1,2")
                        .param("rangeStart", "2023-01-01 00:00:00")
                        .param("rangeEnd", "2023-12-31 23:59:59")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventDto.getTitle()));

        Mockito.verify(eventService).searchAdmin(any());
    }

    @Test
    void getAllEventsWithMinimalParamsShouldReturnOk() throws Exception {
        Mockito.when(eventService.searchAdmin(any()))
                .thenReturn(List.of(eventDto));

        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        Mockito.verify(eventService).searchAdmin(any());
    }

    @Test
    void getAllEventsWithInvalidFromParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllEventsWithInvalidSizeParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/admin/events")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateEventWhenValidShouldReturnOk() throws Exception {
        Mockito.when(eventService.updateByAdmin(anyLong(), any()))
                .thenReturn(eventDto);

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventDto.getId()))
                .andExpect(jsonPath("$.title").value(eventDto.getTitle()));

        Mockito.verify(eventService).updateByAdmin(anyLong(), any());
    }

    @Test
    void updateEventWhenInvalidBodyShouldReturnBadRequest() throws Exception {
        EventDto invalidRequest = new EventDto();
        invalidRequest.setTitle(""); // Пустой заголовок - невалидный

        mockMvc.perform(patch("/admin/events/{eventId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }
}