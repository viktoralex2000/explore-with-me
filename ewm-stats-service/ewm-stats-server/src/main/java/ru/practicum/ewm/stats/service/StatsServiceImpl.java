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
        if (dto == null) {
            throw new IllegalArgumentException("EndpointHitDto is null");
        }

        statsRepository.save(StatsMapper.toEndpointHit(dto));
    }

    @Override
    public List<ViewStatsDto> getStats(String start,
                                       String end,
                                       List<String> uris,
                                       boolean unique) {

        LocalDateTime startDate = parseDate(start);
        LocalDateTime endDate = parseDate(end);

        if (uris != null && uris.isEmpty()) {
            uris = null;
        }

        List<ViewStatsDto> result;

        if (uris == null) {
            result = unique
                    ? statsRepository.findAllUniqueStats(startDate, endDate)
                    : statsRepository.findAllStats(startDate, endDate);
        } else {
            result = unique
                    ? statsRepository.findUniqueStatsByUris(startDate, endDate, uris)
                    : statsRepository.findStatsByUris(startDate, endDate, uris);
        }
        return result == null ? List.of() : result;
    }

    private LocalDateTime parseDate(String value) {
        try {
            return LocalDateTime.parse(value, FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.parse(value);
        }
    }
}


/*package ru.practicum.ewm.stats.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.stats.mapper.StatsMapper;
import ru.practicum.ewm.stats.model.EndpointHit;
import ru.practicum.ewm.stats.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatsServiceImpl implements StatsService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final StatsRepository statsRepository;

    @Override
    @Transactional
    public void hit(EndpointHitDto endpointHitDto) {
        EndpointHit hit = StatsMapper.toEndpointHit(endpointHitDto);
        statsRepository.save(hit);
    }

    @Override
    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, boolean unique) {
        LocalDateTime startDate = LocalDateTime.parse(start, FORMATTER);
        LocalDateTime endDate = LocalDateTime.parse(end, FORMATTER);

        if (uris == null || uris.isEmpty()) {
            if (unique) {
                return statsRepository.findAllUniqueStats(startDate, endDate);
            }
            return statsRepository.findAllStats(startDate, endDate);
        }

        if (unique) {
            return statsRepository.findUniqueStatsByUris(startDate, endDate, uris);
        }
        return statsRepository.findStatsByUris(startDate, endDate, uris);
    }
}*/