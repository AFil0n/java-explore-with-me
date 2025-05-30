package ru.practicum.category.model;

public class CategoryMapper {
    public static Category requestToCategory(CategoryDto categoryRequest) {
        return Category.builder()
                .name(categoryRequest.getName())
                .build();
    }

    public static CategoryDto categoryToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static Category dtoToCategory(CategoryDto dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .build();
    }
}
