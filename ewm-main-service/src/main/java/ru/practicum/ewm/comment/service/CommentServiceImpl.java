package ru.practicum.ewm.comment.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.RejectCommentRequest;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;
import ru.practicum.ewm.comment.mapper.CommentMapper;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.comment.model.CommentState;
import ru.practicum.ewm.comment.repository.CommentRepository;
import ru.practicum.ewm.event.model.Event;
import ru.practicum.ewm.event.model.EventState;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.ConflictException;
import ru.practicum.ewm.exception.ForbiddenException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.user.model.User;
import ru.practicum.ewm.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long eventId, NewCommentDto dto) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " was not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        if (event.getState() != EventState.PUBLISHED) {
            throw new ConflictException("Cannot add comment to unpublished event");
        }

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());
        comment.setState(CommentState.PENDING);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " was not found");
        }

        Comment comment = getComment(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("User is not the author of this comment");
        }

        if (comment.getState() == CommentState.PUBLISHED) {
            throw new ConflictException("Cannot edit already published comment");
        }

        comment.setText(dto.getText());
        comment.setUpdated(LocalDateTime.now());
        comment.setState(CommentState.PENDING);
        comment.setRejectionReason(null);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getComment(commentId);

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("User is not the author of this comment");
        }

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = getComment(commentId);
        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public CommentDto approveComment(Long commentId) {
        Comment comment = getComment(commentId);

        if (comment.getState() == CommentState.PUBLISHED) {
            throw new ConflictException("Comment is already published");
        }

        comment.setState(CommentState.PUBLISHED);
        comment.setRejectionReason(null);

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional
    public CommentDto rejectComment(Long commentId, RejectCommentRequest request) {
        Comment comment = getComment(commentId);

        if (comment.getState() == CommentState.PUBLISHED) {
            throw new ConflictException("Cannot reject already published comment");
        }

        comment.setState(CommentState.REJECTED);
        comment.setRejectionReason(request.getRejectionReason());

        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getCommentsByEvent(Long eventId, int from, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " was not found"));

        Pageable pageable = PageRequest.of(from / size, size);

        return commentRepository.findAllForModeration(CommentState.PUBLISHED, eventId, pageable)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsForModeration(String state, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        CommentState commentState = state != null ? CommentState.valueOf(state) : null;

        return commentRepository.findAllForModeration(commentState, null, pageable)
                .stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    private Comment getComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " was not found"));
    }
}