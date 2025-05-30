package ru.practicum.category.controller;

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
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.extention.NotFoundException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;


@SpringBootTest
@AutoConfigureMockMvc
class AdminCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        categoryDto = new CategoryDto();
        categoryDto.setName("Концерты");

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Концерты");
    }

    @Test
    void createCategoryShouldReturnCreated() throws Exception {
        Mockito.when(categoryService.create(any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService).create(any(CategoryDto.class));
    }

    @Test
    void createCategoryWithInvalidDataShouldReturnBadRequest() throws Exception {
        CategoryDto invalidDto = new CategoryDto(); // name is required

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).create(any(CategoryDto.class));
    }

    @Test
    void updateCategoryShouldReturnUpdatedCategory() throws Exception {
        Mockito.when(categoryService.update(anyLong(), any(CategoryDto.class))).thenReturn(categoryDto);

        mockMvc.perform(patch("/admin/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        verify(categoryService).update(1L, categoryDto);
    }

    @Test
    void updateCategoryWithInvalidDataShouldReturnBadRequest() throws Exception {
        CategoryDto invalidDto = new CategoryDto(); // name is required

        mockMvc.perform(patch("/admin/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).update(anyLong(), any(CategoryDto.class));
    }

    @Test
    void deleteCategoryShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/admin/categories/{id}", 1L))
                .andExpect(status().isNoContent());

        verify(categoryService).delete(1L);
    }

    @Test
    void deleteCategoryWithInvalidIdShouldReturnBadRequest() throws Exception {
        Long nonExistentCategoryId = 999L;
        doThrow(new NotFoundException("Category not found"))
                .when(categoryService).delete(nonExistentCategoryId);

        mockMvc.perform(delete("/admin/categories/{id}", nonExistentCategoryId))
                .andExpect(status().isNotFound());

        Mockito.verify(categoryService).delete(nonExistentCategoryId);
    }
}
