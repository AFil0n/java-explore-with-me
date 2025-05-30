package ru.practicum.category.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.event.model.Event;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private CategoryDto newCategoryDto;
    private CategoryDto categoryDto;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Концерты");

        newCategoryDto = new CategoryDto();
        newCategoryDto.setName("Концерты");

        categoryDto = new CategoryDto();
        categoryDto.setId(1L);
        categoryDto.setName("Концерты");
    }

    @Test
    void getAllShouldReturnListOfCategories() {
        when(categoryRepository.findCategories(0, 10)).thenReturn(List.of(category));

        List<CategoryDto> result = categoryService.getAll(0, 10);

        assertEquals(1, result.size());
        assertEquals(categoryDto.getId(), result.getFirst().getId());
        assertEquals(categoryDto.getName(), result.getFirst().getName());
        verify(categoryRepository).findCategories(0, 10);
    }

    @Test
    void getByIdWhenExistsShouldReturnCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryDto result = categoryService.getById(1L);

        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getByIdWhenNotExistsShouldThrowNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.getById(999L)
        );
        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
    }

    @Test
    void createWhenValidShouldReturnCreatedCategory() {
        when(categoryRepository.findByNameIgnoreCase("Концерты")).thenReturn(List.of());
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(category);

        CategoryDto result = categoryService.create(newCategoryDto);

        assertEquals(categoryDto.getId(), result.getId());
        assertEquals(categoryDto.getName(), result.getName());
        verify(categoryRepository).findByNameIgnoreCase("Концерты");
        verify(categoryRepository).saveAndFlush(any(Category.class));
    }

    @Test
    void createWhenNameExistsShouldThrowConditionsNotMetException() {
        when(categoryRepository.findByNameIgnoreCase("Концерты")).thenReturn(List.of(category));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> categoryService.create(newCategoryDto)
        );
        assertEquals("Категория с именем Концерты уже существует", exception.getMessage());
        verify(categoryRepository).findByNameIgnoreCase("Концерты");
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void updateWhenValidShouldReturnUpdatedCategory() {
        Category updatedCategory = new Category();
        updatedCategory.setId(1L);
        updatedCategory.setName("Обновленные концерты");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Обновленные концерты")).thenReturn(List.of());
        when(categoryRepository.saveAndFlush(any(Category.class))).thenReturn(updatedCategory);

        CategoryDto updateDto = new CategoryDto();
        updateDto.setName("Обновленные концерты");

        CategoryDto result = categoryService.update(1L, updateDto);

        assertEquals(1L, result.getId());
        assertEquals("Обновленные концерты", result.getName());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findByNameIgnoreCase("Обновленные концерты");
        verify(categoryRepository).saveAndFlush(any(Category.class));
    }

    @Test
    void updateWhenCategoryNotExistsShouldThrowNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.update(999L, newCategoryDto)
        );
        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void updateWhenNameExistsForOtherCategoryShouldThrowConditionsNotMetException() {
        Category otherCategory = new Category();
        otherCategory.setId(2L);
        otherCategory.setName("Концерты");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.findByNameIgnoreCase("Концерты")).thenReturn(List.of(otherCategory));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> categoryService.update(1L, newCategoryDto)
        );
        assertEquals("Категория с именем Концерты уже существует", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).findByNameIgnoreCase("Концерты");
        verify(categoryRepository, never()).saveAndFlush(any(Category.class));
    }

    @Test
    void deleteWhenValidShouldDeleteCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.findAllByCategoryId(1L)).thenReturn(List.of());

        categoryService.delete(1L);

        verify(categoryRepository).findById(1L);
        verify(eventRepository).findAllByCategoryId(1L);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteWhenCategoryNotExistsShouldThrowNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> categoryService.delete(999L)
        );
        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(categoryRepository).findById(999L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteWhenCategoryHasEventsShouldThrowConditionsNotMetException() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.findAllByCategoryId(1L)).thenReturn(List.of(new Event()));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> categoryService.delete(1L)
        );
        assertEquals("Удаление категории невозможно, так как она используется в событиях", exception.getMessage());
        verify(categoryRepository).findById(1L);
        verify(eventRepository).findAllByCategoryId(1L);
        verify(categoryRepository, never()).deleteById(anyLong());
    }


}
