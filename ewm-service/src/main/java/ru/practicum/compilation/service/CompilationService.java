package ru.practicum.compilation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.model.CompilationDto;
import ru.practicum.compilation.model.CompilationMapper;
import ru.practicum.compilation.model.NewCompilationDto;
import ru.practicum.compilation.model.UpdateCompilationRequest;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.NotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    public List<CompilationDto> findAll(Boolean pinned, Integer from, Integer size) {
        return compilationRepository.findCompilations(pinned, from, size).stream().map(CompilationMapper::toCompilationDto).toList();
    }

    public CompilationDto findById(Long compId) {
        return compilationRepository.findById(compId)
                .map(CompilationMapper::toCompilationDto)
                .orElse(null);
    }

    public CompilationDto create(NewCompilationDto compilationDto) {
        if (!compilationRepository.findByTitleIgnoreCase(compilationDto.getTitle()).isEmpty()) {
            throw new ConditionsNotMetException("Подборка с названием " + compilationDto.getTitle() + " уже существует");
        }

        Set<Event> events = new HashSet<>();
        if (compilationDto.getEvents() != null && !compilationDto.getEvents().isEmpty()) {
            events = new HashSet<>(eventRepository.findAllByIdIn(compilationDto.getEvents().stream().toList()));
        }

        return CompilationMapper.toCompilationDto(
                compilationRepository.save(CompilationMapper.newCompilationDtoToCompilation(compilationDto, events))
        );
    }

    public CompilationDto update(Long compilationId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation compilation = compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException("Подборка с id=" + compilationId + " не найдена"));

        if (updateCompilationRequest.getTitle() != null) {
            if (!compilationRepository.findByTitleIgnoreCase(updateCompilationRequest.getTitle()).isEmpty() &&
                    !compilation.getTitle().equalsIgnoreCase(updateCompilationRequest.getTitle())) {
                throw new ConditionsNotMetException("Подборка с названием " + updateCompilationRequest.getTitle() + " уже существует");
            }
            compilation.setTitle(updateCompilationRequest.getTitle());
        }

        if (updateCompilationRequest.getEvents() != null) {
            Set<Event> events = new HashSet<>();
            if (!updateCompilationRequest.getEvents().isEmpty()) {
                events = new HashSet<>(eventRepository.findAllByIdIn(updateCompilationRequest.getEvents().stream().toList()));
            }
            compilation.setEvents(events);
        }

        if (updateCompilationRequest.getPinned() != null) {
            compilation.setPinned(updateCompilationRequest.getPinned());
        }

        return CompilationMapper.toCompilationDto(compilationRepository.save(compilation));
    }

    public void delete(Long compilationId) {
        compilationRepository.findById(compilationId).orElseThrow(() -> new NotFoundException("Подборка c id=" + compilationId + " не найдена"));
        compilationRepository.deleteById(compilationId);
    }
}
