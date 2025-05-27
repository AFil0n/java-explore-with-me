package ru.practicum.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.dto.StatsDto;
import ru.practicum.model.StatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;
import ru.practicum.utils.SimpleDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private StatsRepository endpointHitsRepository;

    @InjectMocks
    private ru.practicum.service.StatsService statsService;

    private EndpointHitDto hitDto;
    private EndpointHit hit;
    private StatsDto statsDto;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();

        hitDto = new EndpointHitDto();
        hitDto.setApp("test-app");
        hitDto.setUri("/test");
        hitDto.setIp("127.0.0.1");
        hitDto.setTimestamp(SimpleDateTimeFormatter.toString(now));

        hit = new EndpointHit();
        hit.setId(1L);
        hit.setApp("test-app");
        hit.setUri("/test");
        hit.setIp("127.0.0.1");
        hit.setTimestamp(now);

        statsDto = new StatsDto();
        statsDto.setApp("test-app");
        statsDto.setUri("/test");
        statsDto.setHits(10L);
    }

    @Test
    void hitShouldSaveAndReturnDto() {
        when(endpointHitsRepository.save(any(EndpointHit.class))).thenReturn(hit);

        EndpointHitDto result = statsService.hit(hitDto);

        assertNotNull(result);
        assertEquals("test-app", result.getApp());
        assertEquals("/test", result.getUri());
        verify(endpointHitsRepository, times(1)).save(any(EndpointHit.class));
    }

    @Test
    void getWithUniqueFalseShouldReturnStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/test");

        when(endpointHitsRepository.findStats(start, end, uris))
                .thenReturn(List.of(StatsMapper.toStats(statsDto)));

        List<StatsDto> result = statsService.getStats(start, end, uris, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-app", result.getFirst().getApp());
        assertEquals(10L, result.getFirst().getHits());
        verify(endpointHitsRepository, times(1)).findStats(start, end, uris);
    }

    @Test
    void getWithUniqueTrueShouldReturnUniqueStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        List<String> uris = List.of("/test");

        when(endpointHitsRepository.findUniqueStats(start, end, uris))
                .thenReturn(List.of(StatsMapper.toStats(statsDto)));

        List<StatsDto> result = statsService.getStats(start, end, uris, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-app", result.getFirst().getApp());
        assertEquals(10L, result.getFirst().getHits());
        verify(endpointHitsRepository, times(1)).findUniqueStats(start, end, uris);
    }

    @Test
    void getWithoutUrisShouldReturnAllStats() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();

        when(endpointHitsRepository.findStats(start, end, null))
                .thenReturn(List.of(StatsMapper.toStats(statsDto)));

        List<StatsDto> result = statsService.getStats(start, end, null, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(endpointHitsRepository, times(1)).findStats(start, end, null);
    }

    @Test
    void getWhenStartAfterEndShouldThrowException() {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> statsService.getStats(start, end, null, false)
        );

        assertEquals("Дата начала не может быть позже даты конца", exception.getMessage());
        verify(endpointHitsRepository, never()).findStats(any(), any(), any());
        verify(endpointHitsRepository, never()).findUniqueStats(any(), any(), any());
    }
}
