package ru.practicum.ewm.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.dto.*;
import ru.practicum.ewm.event.mapper.EventMapper;
import ru.practicum.ewm.event.mapper.LocationMapper;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.model.EventStateAction;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.request.dto.ParticipationRequestDto;
import ru.practicum.ewm.request.mapper.RequestMapper;
import ru.practicum.ewm.request.model.ParticipationRequest;
import ru.practicum.ewm.request.model.RequestStatus;
import ru.practicum.ewm.request.repository.RequestRepository;
import ru.practicum.ewm.stats.client.StatsClient;
import ru.practicum.ewm.stats.dto.EndpointHitDto;
import ru.practicum.ewm.stats.dto.ViewStatsDto;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final RequestRepository requestRepository;
    private final StatsClient statsClient;

    public EventServiceImpl(EventRepository eventRepository,
                            UserRepository userRepository,
                            CategoryRepository categoryRepository,
                            RequestRepository requestRepository,
                            StatsClient statsClient) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.requestRepository = requestRepository;
        this.statsClient = statsClient;
    }

    /*@Override
    public List<EventShortDto> getAllPublic(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            String rangeStart,
                                            String rangeEnd,
                                            boolean onlyAvailable,
                                            String sort,
                                            int from,
                                            int size,
                                            HttpServletRequest request) {

        statsClient.hit(makeHitDto(request));

        LocalDateTime start = rangeStart == null
                ? LocalDateTime.now().minusYears(100)
                : LocalDateTime.parse(rangeStart, FORMATTER);

        LocalDateTime end = rangeEnd == null
                ? LocalDateTime.now().plusYears(100)
                : LocalDateTime.parse(rangeEnd, FORMATTER);

        if (end.isBefore(start)) {
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.searchPublic(
                EventState.PUBLISHED,
                text,
                (categories == null || categories.isEmpty()) ? null : categories,
                paid,
                start,
                end,
                pageable
        ).getContent();

        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() == 0 ||
                            e.getConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        Map<Long, Long> views = getViewsForEvents(events);

        for (Event event : events) {
            event.setViews(views.getOrDefault(event.getId(), 0L));
        }

        List<EventShortDto> result = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        if ("VIEWS".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        } else if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        return result;
    }*/

    @Override
    public List<EventShortDto> getAllPublic(String text,
                                            List<Long> categories,
                                            Boolean paid,
                                            String rangeStart,
                                            String rangeEnd,
                                            boolean onlyAvailable,
                                            String sort,
                                            int from,
                                            int size,
                                            HttpServletRequest request) {

        statsClient.hit(makeHitDto(request));

        LocalDateTime start;
        LocalDateTime end;

        try {
            start = (rangeStart == null || rangeStart.isBlank())
                    ? LocalDateTime.now().minusYears(100)
                    : LocalDateTime.parse(rangeStart, FORMATTER);
        } catch (Exception e) {
            start = LocalDateTime.now().minusYears(100);
        }

        try {
            end = (rangeEnd == null || rangeEnd.isBlank())
                    ? LocalDateTime.now().plusYears(100)
                    : LocalDateTime.parse(rangeEnd, FORMATTER);
        } catch (Exception e) {
            end = LocalDateTime.now().plusYears(100);
        }

        if (end.isBefore(start)) {
            throw new BadRequestException("rangeEnd must be after rangeStart");
        }

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.searchPublic(
                EventState.PUBLISHED,
                text,
                (categories == null || categories.isEmpty()) ? null : categories,
                paid,
                start,
                end,
                pageable
        ).getContent();

        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() == 0 ||
                            e.getConfirmedRequests() < e.getParticipantLimit())
                    .collect(Collectors.toList());
        }

        Map<Long, Long> views = getViewsForEvents(events);

        for (Event event : events) {
            event.setViews(views.getOrDefault(event.getId(), 0L));
        }

        List<EventShortDto> result = events.stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());

        if ("VIEWS".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        } else if ("EVENT_DATE".equalsIgnoreCase(sort)) {
            result.sort(Comparator.comparing(EventShortDto::getEventDate));
        }

        return result;
    }

    @Override
    public EventFullDto getByIdPublic(Long eventId, HttpServletRequest request) {
        statsClient.hit(makeHitDto(request));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new NotFoundException("Event with id=" + eventId + " was not found");
        }

        Long views = getViewsForEvent(eventId);
        event.setViews(views);

        return EventMapper.toEventFullDto(event);
    }

    @Override
    public List<EventShortDto> getAllPrivate(Long userId, int from, int size) {
        checkUser(userId);

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .map(EventMapper::toEventShortDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto create(Long userId, NewEventDto dto) {
        User initiator = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Category category = categoryRepository.findById(dto.getCategory())
                .orElseThrow(() -> new NotFoundException("Category with id=" + dto.getCategory() + " was not found"));

        if (dto.getEventDate().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Event date must be at least 2 hours in the future");
        }

        Event event = new Event();
        event.setAnnotation(dto.getAnnotation());
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setDescription(dto.getDescription());
        event.setEventDate(dto.getEventDate());
        event.setInitiator(initiator);
        event.setLocation(LocationMapper.toLocation(dto.getLocation()));
        event.setPaid(dto.getPaid());
        event.setParticipantLimit(dto.getParticipantLimit());
        event.setRequestModeration(dto.getRequestModeration());
        event.setState(EventState.PENDING);
        event.setTitle(dto.getTitle());
        event.setViews(0L);
        event.setConfirmedRequests(0L);

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getByIdPrivate(Long userId, Long eventId) {
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User is not initiator of this event");
        }

        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updatePrivate(Long userId, Long eventId, UpdateEventUserRequest request) {
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User is not initiator of this event");
        }

        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Published event cannot be changed");
        }

        applyUpdate(event, request.getAnnotation(), request.getDescription(), request.getTitle(),
                request.getPaid(), request.getParticipantLimit(), request.getRequestModeration(),
                request.getEventDate(), request.getLocation(), request.getCategory());

        if (request.getStateAction() != null) {
            EventStateAction action = EventStateAction.valueOf(request.getStateAction());
            if (action == EventStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            } else if (action == EventStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    @Override
    public List<ParticipationRequestDto> getRequests(Long userId, Long eventId) {
        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User is not initiator of this event");
        }

        return requestRepository.findAllByEventId(eventId).stream()
                .map(RequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateRequestsStatus(Long userId,
                                                               Long eventId,
                                                               EventRequestStatusUpdateRequest request) {

        Event event = getEventById(eventId);

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("User is not initiator of this event");
        }

        if (event.getParticipantLimit() == 0) {
            throw new ConflictException("Participant limit is 0");
        }

        if (request.getRequestIds() == null || request.getRequestIds().isEmpty()) {
            throw new BadRequestException("requestIds must not be empty");
        }

        RequestStatus status = RequestStatus.valueOf(request.getStatus());

        List<ParticipationRequest> requests = requestRepository.findAllByIdIn(request.getRequestIds());

        List<ParticipationRequestDto> confirmed = new ArrayList<>();
        List<ParticipationRequestDto> rejected = new ArrayList<>();

        long confirmedCount = requestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        for (ParticipationRequest pr : requests) {
            if (!pr.getEvent().getId().equals(eventId)) {
                continue;
            }

            if (pr.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Request must have status PENDING");
            }

            if (status == RequestStatus.CONFIRMED) {
                if (event.getParticipantLimit() != 0 && confirmedCount >= event.getParticipantLimit()) {
                    pr.setStatus(RequestStatus.REJECTED);
                    rejected.add(RequestMapper.toParticipationRequestDto(pr));
                } else {
                    pr.setStatus(RequestStatus.CONFIRMED);
                    confirmed.add(RequestMapper.toParticipationRequestDto(pr));
                    confirmedCount++;
                }
            } else if (status == RequestStatus.REJECTED) {
                pr.setStatus(RequestStatus.REJECTED);
                rejected.add(RequestMapper.toParticipationRequestDto(pr));
            }
        }

        requestRepository.saveAll(requests);

        event.setConfirmedRequests(confirmedCount);
        eventRepository.save(event);

        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        result.setConfirmedRequests(confirmed);
        result.setRejectedRequests(rejected);

        return result;
    }

    @Override
    public List<EventFullDto> getAllAdmin(List<Long> users,
                                          List<String> states,
                                          List<Long> categories,
                                          String rangeStart,
                                          String rangeEnd,
                                          int from,
                                          int size) {

        LocalDateTime start = rangeStart == null
                ? LocalDateTime.now().minusYears(100)
                : LocalDateTime.parse(rangeStart, FORMATTER);

        LocalDateTime end = rangeEnd == null
                ? LocalDateTime.now().plusYears(100)
                : LocalDateTime.parse(rangeEnd, FORMATTER);

        Pageable pageable = PageRequest.of(from / size, size);

        List<EventState> parsedStates = null;
        if (states != null && !states.isEmpty()) {
            parsedStates = states.stream()
                    .map(EventState::valueOf)
                    .collect(Collectors.toList());
        }

        return eventRepository.searchAdmin(
                        (users == null || users.isEmpty()) ? null : users,
                        parsedStates,
                        (categories == null || categories.isEmpty()) ? null : categories,
                        start,
                        end,
                        pageable
                ).getContent().stream()
                .map(EventMapper::toEventFullDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto updateAdmin(Long eventId, UpdateEventAdminRequest request) {
        Event event = getEventById(eventId);

        applyUpdate(event, request.getAnnotation(), request.getDescription(), request.getTitle(),
                request.getPaid(), request.getParticipantLimit(), request.getRequestModeration(),
                request.getEventDate(), request.getLocation(), request.getCategory());

        if (request.getStateAction() != null) {
            EventStateAction action = EventStateAction.valueOf(request.getStateAction());

            if (action == EventStateAction.PUBLISH_EVENT) {
                if (event.getState() != EventState.PENDING) {
                    throw new ConflictException("Event must be in PENDING state");
                }
                event.setState(EventState.PUBLISHED);
                event.setPublishedOn(LocalDateTime.now());
            }

            if (action == EventStateAction.REJECT_EVENT) {
                if (event.getState() == EventState.PUBLISHED) {
                    throw new ConflictException("Published event cannot be rejected");
                }
                event.setState(EventState.CANCELED);
            }
        }

        return EventMapper.toEventFullDto(eventRepository.save(event));
    }

    private void applyUpdate(Event event,
                             String annotation,
                             String description,
                             String title,
                             Boolean paid,
                             Integer participantLimit,
                             Boolean requestModeration,
                             String eventDate,
                             LocationDto location,
                             Long categoryId) {

        if (annotation != null) {
            event.setAnnotation(annotation);
        }
        if (description != null) {
            event.setDescription(description);
        }
        if (title != null) {
            event.setTitle(title);
        }
        if (paid != null) {
            event.setPaid(paid);
        }
        if (participantLimit != null) {
            event.setParticipantLimit(participantLimit);
        }
        if (requestModeration != null) {
            event.setRequestModeration(requestModeration);
        }
        if (eventDate != null) {
            LocalDateTime parsedDate = LocalDateTime.parse(eventDate, FORMATTER);

            if (parsedDate.isBefore(LocalDateTime.now().plusHours(2))) {
                throw new BadRequestException("Event date must be at least 2 hours in the future");
            }

            event.setEventDate(parsedDate);
        }
        if (location != null) {
            event.setLocation(LocationMapper.toLocation(location));
        }
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new NotFoundException("Category with id=" + categoryId + " was not found"));
            event.setCategory(category);
        }
    }

    private Event getEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));
    }

    private void checkUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }
    }

    private EndpointHitDto makeHitDto(HttpServletRequest request) {
        EndpointHitDto dto = new EndpointHitDto();
        dto.setApp("ewm-main-service");
        dto.setUri(request.getRequestURI());
        dto.setIp(request.getRemoteAddr());
        dto.setTimestamp(LocalDateTime.now().format(FORMATTER));
        return dto;
    }

    private Map<Long, Long> getViewsForEvents(List<Event> events) {
        if (events.isEmpty()) {
            return Collections.emptyMap();
        }

        String start = LocalDateTime.now().minusYears(100).format(FORMATTER);
        String end = LocalDateTime.now().plusYears(100).format(FORMATTER);

        List<String> uris = events.stream()
                .map(e -> "/events/" + e.getId())
                .collect(Collectors.toList());

        List<ViewStatsDto> stats = statsClient.getStats(start, end, uris, false);

        Map<Long, Long> result = new HashMap<>();
        for (ViewStatsDto dto : stats) {
            String uri = dto.getUri();
            if (uri != null && uri.startsWith("/events/")) {
                Long id = Long.parseLong(uri.substring("/events/".length()));
                result.put(id, dto.getHits());
            }
        }
        return result;
    }

    private Long getViewsForEvent(Long eventId) {
        String start = LocalDateTime.now().minusYears(100).format(FORMATTER);
        String end = LocalDateTime.now().plusYears(100).format(FORMATTER);

        List<ViewStatsDto> stats = statsClient.getStats(start, end, List.of("/events/" + eventId), false);

        if (stats.isEmpty()) {
            return 0L;
        }

        return stats.get(0).getHits();
    }
}