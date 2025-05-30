package ru.practicum.compitation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.model.Category;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    private Event event;

    @Test
    void createShouldReturnCreatedCompilation() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setInitiator(user);
        event.setCategory(category);
        event.setPaid(false);
        event.setParticipantLimit(10L);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLat(55.754167);
        event.setLon(37.620000);
        NewCompilationDto newCompilationDto = new NewCompilationDto();
        newCompilationDto.setTitle("New Compilation");
        newCompilationDto.setPinned(true);
        newCompilationDto.setEvents(Set.of(event.getId()));

        CompilationDto expectedDto = new CompilationDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("New Compilation");
        expectedDto.setPinned(true);
        expectedDto.setEvents(Collections.emptyList());

        Mockito.when(compilationService.create(any(NewCompilationDto.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newCompilationDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("New Compilation"))
                .andExpect(jsonPath("$.pinned").value(true));
    }

    @Test
    void createShouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        CompilationDto invalidDto = new CompilationDto();
        invalidDto.setTitle(" ");
        invalidDto.setPinned(false);

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(compilationService).delete(anyLong());

        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void updateShouldReturnUpdatedCompilation() throws Exception {
        UpdateCompilationRequest updateRequest = new UpdateCompilationRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setPinned(true);
        updateRequest.setEvents(Collections.emptySet());

        CompilationDto expectedDto = new CompilationDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("Updated Title");
        expectedDto.setPinned(true);
        expectedDto.setEvents(Collections.emptyList());

        Mockito.when(compilationService.update(anyLong(), any(UpdateCompilationRequest.class)))
                .thenReturn(expectedDto);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.pinned").value(true));
    }
}
