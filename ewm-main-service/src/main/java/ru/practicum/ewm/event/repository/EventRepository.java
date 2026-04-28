package ru.practicum.ewm.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    Page<Event> findAllByInitiatorId(Long userId, Pageable pageable);

    @Query("""
            select e from Event e
            where e.state = :state
              and (:text is null or (lower(e.annotation) like lower(concat('%', :text, '%'))
                    or lower(e.description) like lower(concat('%', :text, '%'))))
              and (:categories is null or e.category.id in :categories)
              and (:paid is null or e.paid = :paid)
              and (e.eventDate >= :rangeStart)
              and (e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchPublic(EventState state,
                             String text,
                             List<Long> categories,
                             Boolean paid,
                             LocalDateTime rangeStart,
                             LocalDateTime rangeEnd,
                             Pageable pageable);

    @Query("""
            select e from Event e
            where (:users is null or e.initiator.id in :users)
              and (:states is null or e.state in :states)
              and (:categories is null or e.category.id in :categories)
              and (e.eventDate >= :rangeStart)
              and (e.eventDate <= :rangeEnd)
            """)
    Page<Event> searchAdmin(List<Long> users,
                            List<EventState> states,
                            List<Long> categories,
                            LocalDateTime rangeStart,
                            LocalDateTime rangeEnd,
                            Pageable pageable);
}