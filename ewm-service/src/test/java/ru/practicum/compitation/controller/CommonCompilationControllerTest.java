package ru.practicum.compitation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.service.CompilationService;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommonCompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    @Test
    void findAllShouldReturnListOfCompilations() throws Exception {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(1L);
        compilationDto.setTitle("Test Compilation");
        compilationDto.setPinned(true);
        compilationDto.setEvents(Collections.emptyList());

        Mockito.when(compilationService.findAll(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(List.of(compilationDto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].title").value("Test Compilation"))
                .andExpect(jsonPath("$[0].pinned").value(true));
    }

    @Test
    void findAllShouldUseDefaultParameters() throws Exception {
        Mockito.when(compilationService.findAll(isNull(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk());

        Mockito.verify(compilationService).findAll(null, 0, 10);
    }

    @Test
    void findAllShouldReturnEmptyList() throws Exception {
        Mockito.when(compilationService.findAll(anyBoolean(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/compilations")
                        .param("pinned", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void findByIdShouldReturnCompilation() throws Exception {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(1L);
        compilationDto.setTitle("Test Compilation");
        compilationDto.setPinned(false);
        compilationDto.setEvents(Collections.emptyList());

        Mockito.when(compilationService.findById(anyLong()))
                .thenReturn(compilationDto);

        mockMvc.perform(get("/compilations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Compilation"))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void findByIdShouldReturnNotFoundForInvalidId() throws Exception {
        Mockito.when(compilationService.findById(anyLong()))
                .thenThrow(new ru.practicum.extention.NotFoundException("Compilation not found"));

        mockMvc.perform(get("/compilations/999"))
                .andExpect(status().isNotFound());
    }
}
