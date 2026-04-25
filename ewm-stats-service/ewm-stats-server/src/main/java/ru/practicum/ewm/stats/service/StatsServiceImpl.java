package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void hit(EndpointHitDto dto) {
        statsRepository.save(StatsMapper.toEndpointHit(dto));
    }

    @Override
    public List<ViewStatsDto> getStats(String start,
                                       String end,
                                       List<String> uris,
                                       boolean unique) {

        LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

        if (uris != null && uris.isEmpty()) {
            uris = null;
        }

        if (uris == null) {
            return unique
                    ? statsRepository.findAllUniqueStats(startDate, endDate)
                    : statsRepository.findAllStats(startDate, endDate);
        }

        return unique
                ? statsRepository.findUniqueStatsByUris(startDate, endDate, uris)
                : statsRepository.findStatsByUris(startDate, endDate, uris);
    }
}
