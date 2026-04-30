package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;

import java.util.List;

public interface EventService {

    // PUBLIC
    List<EventShortDto> getAllPublic(String text,
                                     List<Long> categories,
                                     Boolean paid,
                                     boolean onlyAvailable,
                                     String sort,
                                     int from,
                                     int size,
                                     HttpServletRequest request);

    EventFullDto getByIdPublic(Long eventId, HttpServletRequest request);

    // PRIVATE
    List<EventShortDto> getAllPrivate(Long userId, int from, int size);

    EventFullDto create(Long userId, NewEventDto dto);

    EventFullDto getByIdPrivate(Long userId, Long eventId);

    EventFullDto updatePrivate(Long userId, Long eventId, UpdateEventUserRequest request);

    List<ParticipationRequestDto> getRequests(Long userId, Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(Long userId,
                                                        Long eventId,
                                                        EventRequestStatusUpdateRequest request);

    // ADMIN
    List<EventFullDto> getAllAdmin(List<Long> users,
                                   List<String> states,
                                   List<Long> categories,
                                   String rangeStart,
                                   String rangeEnd,
                                   int from,
                                   int size);

    EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest request);
}