package ru.practicum.category.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query("SELECT c FROM categories c")
    Page<Category> findCategories(Pageable pageable);

    default List<Category> findCategories(Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findCategories(pageable).toList();
    }

    List<Category> findByNameIgnoreCase(String name);
}
