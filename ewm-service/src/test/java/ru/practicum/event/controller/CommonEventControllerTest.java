package ru.practicum.event.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.event.model.EventDto;
import ru.practicum.event.service.EventService;
import ru.practicum.extention.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommonEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    private EventDto eventShortDto;
    private EventDto eventFullDto;

    @BeforeEach
    void setUp() {
        eventShortDto = new EventDto();
        eventShortDto.setId(1L);
        eventShortDto.setTitle("Test Event Short");
        eventShortDto.setAnnotation("Short Annotation");

        eventFullDto = new EventDto();
        eventFullDto.setId(1L);
        eventFullDto.setTitle("Test Event Full");
        eventFullDto.setAnnotation("Full Annotation");
        eventFullDto.setDescription("Full Description");
        eventFullDto.setEventDate(LocalDateTime.now().plusDays(1));
    }

    @Test
    void findAllWithAllParamsShouldReturnOk() throws Exception {
        Mockito.when(eventService.searchCommon(any()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events")
                        .param("text", "test")
                        .param("categories", "1,2")
                        .param("paid", "true")
                        .param("rangeStart", "2023-01-01 00:00:00")
                        .param("rangeEnd", "2023-12-31 23:59:59")
                        .param("onlyAvailable", "true")
                        .param("sort", "EVENT_DATE")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventShortDto.getId()))
                .andExpect(jsonPath("$[0].title").value(eventShortDto.getTitle()));

        Mockito.verify(eventService).searchCommon(any());
    }

    @Test
    void findAllWithMinimalParamsShouldReturnOk() throws Exception {
        Mockito.when(eventService.searchCommon(any()))
                .thenReturn(List.of(eventShortDto));

        mockMvc.perform(get("/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());

        Mockito.verify(eventService).searchCommon(any());
    }

    @Test
    void findAllWithInvalidFromParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "-1")
                        .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findAllWithInvalidSizeParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/events")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findByIdWhenValidShouldReturnOk() throws Exception {
        Mockito.when(eventService.findById(anyLong()))
                .thenReturn(eventFullDto);

        mockMvc.perform(get("/events/{eventId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventFullDto.getId()))
                .andExpect(jsonPath("$.title").value(eventFullDto.getTitle()));

        Mockito.verify(eventService).findById(1L);
    }

    @Test
    void findByIdWhenEventNotPublishedShouldReturnNotFound() throws Exception {
        Mockito.when(eventService.findById(anyLong()))
                .thenThrow(new NotFoundException("Событие не найдено"));

        mockMvc.perform(get("/events/{eventId}", 2L))
                .andExpect(status().isNotFound());

        Mockito.verify(eventService).findById(2L);
    }
}
