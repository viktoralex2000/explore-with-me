package ru.practicum.ewm.comment.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentState;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findAllByEventIdAndState(Long eventId, CommentState state, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE " +
           "(:state IS NULL OR c.state = :state) AND " +
           "(:eventId IS NULL OR c.event.id = :eventId)")
    Page<Comment> findAllForModeration(@Param("state") CommentState state,
                                       @Param("eventId") Long eventId,
                                       Pageable pageable);
}