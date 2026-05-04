package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query(value = "SELECT * FROM events e " +
                   "WHERE e.state = :state " +
                   "AND (CAST(:text AS text) IS NULL OR " +
                   "(LOWER(e.annotation) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%')) OR " +
                   "LOWER(e.description) LIKE LOWER(CONCAT('%', CAST(:text AS text), '%')))) " +
                   "AND (CAST(:categories AS text) IS NULL OR e.category_id IN (:categories)) " +
                   "AND (CAST(:paid AS boolean) IS NULL OR e.paid = :paid) " +
                   "AND (CAST(:rangeStart AS timestamp) IS NULL OR e.event_date >= CAST(:rangeStart AS timestamp)) " +
                   "AND (CAST(:rangeEnd AS timestamp) IS NULL OR e.event_date <= CAST(:rangeEnd AS timestamp))", nativeQuery = true)
    Page<Event> searchPublic(@Param("state") String state,
                             @Param("text") String text,
                             @Param("categories") List<Long> categories,
                             @Param("paid") Boolean paid,
                             @Param("rangeStart") LocalDateTime rangeStart,
                             @Param("rangeEnd") LocalDateTime rangeEnd,
                             Pageable pageable);

    @Query("select e from Event e " +
           "where (:users is null or e.initiator.id in :users) " +
           "and (:states is null or e.state in :states) " +
           "and (:categories is null or e.category.id in :categories) " +
           "and (e.eventDate >= :rangeStart) " +
           "and (e.eventDate <= :rangeEnd)")
    Page<Event> searchAdmin(@Param("users") List<Long> users,
                            @Param("states") List<EventState> states,
                            @Param("categories") List<Long> categories,
                            @Param("rangeStart") LocalDateTime rangeStart,
                            @Param("rangeEnd") LocalDateTime rangeEnd,
                            Pageable pageable);
}