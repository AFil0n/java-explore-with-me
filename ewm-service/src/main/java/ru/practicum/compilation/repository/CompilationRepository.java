package ru.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;

import java.util.List;

@Repository
public interface CompilationRepository extends JpaRepository<Compilation, Long> {
    List<Compilation> findByTitleIgnoreCase(String title);

    @Query("SELECT c FROM compilations c " +
            "WHERE (:pinned IS NULL OR c.pinned = :pinned)")
    List<Compilation> findCompilations(Boolean pinned, Pageable pageable);

    default List<Compilation> findCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();
        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }
        return findCompilations(pinned, pageable);
    }
}
