package ru.practicum.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.category.model.Category;
import ru.practicum.category.model.CategoryDto;
import ru.practicum.category.model.CategoryMapper;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    public List<CategoryDto> getAll(Integer from, Integer size) {
        return categoryRepository.findCategories(from, size).stream().map(CategoryMapper::categoryToDto).toList();
    }

    public CategoryDto getById(Long id) {
        return CategoryMapper.categoryToDto(
                categoryRepository.findById(id).orElseThrow(
                        () -> new NotFoundException("Категория с id=" + id + " не найдена")
                )
        );
    }

    public CategoryDto create(CategoryDto newCategoryDto) {
        if (!categoryRepository.findByNameIgnoreCase(newCategoryDto.getName()).isEmpty()) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        return CategoryMapper.categoryToDto(
                categoryRepository.saveAndFlush(CategoryMapper.requestToCategory(newCategoryDto))
        );
    }

    public CategoryDto update(Long id, CategoryDto newCategoryDto) {
        Category category = categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        List<Category> categories = categoryRepository.findByNameIgnoreCase(newCategoryDto.getName());
        if (!categories.isEmpty() && !categories.getFirst().getId().equals(id)) {
            throw new ConditionsNotMetException("Категория с именем " + newCategoryDto.getName() + " уже существует");
        }

        category.setName(newCategoryDto.getName());
        return CategoryMapper.categoryToDto(categoryRepository.saveAndFlush(category));
    }

    public void delete(Long id) {
        categoryRepository.findById(id).orElseThrow(() ->
                new NotFoundException("Категория с id=" + id + " не найдена")
        );
        if (!eventRepository.findAllByCategoryId(id).isEmpty()) {
            throw new ConditionsNotMetException("Удаление категории невозможно, так как она используется в событиях");
        }
        categoryRepository.deleteById(id);
    }
}
