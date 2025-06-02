package ru.practicum.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.service.CategoryService;
import ru.practicum.extention.NotFoundException;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class CommonCategoryControllerTest {

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
        categoryDto.setId(1L);
        categoryDto.setName("Концерты");
    }

    @Test
    void getAllCategoriesShouldReturnListOfCategories() throws Exception {
        List<CategoryDto> categories = List.of(categoryDto);
        when(categoryService.getAll(0, 10)).thenReturn(categories);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(categoryDto.getId()))
                .andExpect(jsonPath("$[0].name").value(categoryDto.getName()));

        Mockito.verify(categoryService).getAll(0, 10);
    }

    @Test
    void getAllCategoriesWithPaginationShouldUseParameters() throws Exception {
        List<CategoryDto> categories = List.of(categoryDto);
        when(categoryService.getAll(5, 20)).thenReturn(categories);

        mockMvc.perform(get("/categories")
                        .param("from", "5")
                        .param("size", "20"))
                .andExpect(status().isOk());

        Mockito.verify(categoryService).getAll(5, 20);
    }

    @Test
    void getAllCategoriesWithInvalidFromParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllCategoriesWithInvalidSizeParamShouldReturnBadRequest() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCategoryByIdShouldReturnCategory() throws Exception {
        when(categoryService.getById(1L)).thenReturn(categoryDto);

        mockMvc.perform(get("/categories/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryDto.getId()))
                .andExpect(jsonPath("$.name").value(categoryDto.getName()));

        Mockito.verify(categoryService).getById(1L);
    }

    @Test
    void getCategoryByIdWhenNotExistsShouldReturnNotFound() throws Exception {
        when(categoryService.getById(999L)).thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/categories/{id}", 999L))
                .andExpect(status().isNotFound());

        Mockito.verify(categoryService).getById(999L);
    }
}
