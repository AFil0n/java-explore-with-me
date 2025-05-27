package ru.practicum.model;

import org.springframework.stereotype.Component;
import ru.practicum.dto.StatsDto;

@Component
public class StatsMapper {
    public static StatsDto toStatsDto(Stats stats) {
        return StatsDto.builder()
                .app(stats.getApp())
                .uri(stats.getUri())
                .hits(stats.getHits())
                .build();
    }

    public static Stats toStats(StatsDto statsDto) {
        return Stats.builder()
                .app(statsDto.getApp())
                .uri(statsDto.getUri())
                .hits(statsDto.getHits())
                .build();
    }
}
