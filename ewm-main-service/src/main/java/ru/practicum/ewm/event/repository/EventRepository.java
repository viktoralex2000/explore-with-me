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

    @Query("""
            select e from Event e
            where e.state = :state
              and (:text is null or (lower(cast(e.annotation as text)) like lower(concat('%', :text, '%'))
                    or lower(cast(e.description as text)) like lower(concat('%', :text, '%'))))
              and (:categories is null or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (cast(:rangeStart as timestamp) is null or e.eventDate >= cast(:rangeStart as timestamp))
              and (cast(:rangeEnd as timestamp) is null or e.eventDate <= cast(:rangeEnd as timestamp))
            """)
    Page<Event> searchPublic(@Param("state") EventState state,
                             @Param("text") String text,
                             @Param("categories") List<Long> categories,
                             @Param("paid") Boolean paid,
                             @Param("rangeStart") LocalDateTime rangeStart,
                             @Param("rangeEnd") LocalDateTime rangeEnd,
                             Pageable pageable);

    @Query("""
            select e from Event e
            where (:users is null or e.initiator.id in :users)
              and (:states is null or e.state in :states)
              and (:categories is null or e.category.id in :categories)
              and (e.eventDate >= :rangeStart)
              and (e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchAdmin(@Param("users") List<Long> users,
                            @Param("states") List<EventState> states,
                            @Param("categories") List<Long> categories,
                            @Param("rangeStart") LocalDateTime rangeStart,
                            @Param("rangeEnd") LocalDateTime rangeEnd,
                            Pageable pageable);
}