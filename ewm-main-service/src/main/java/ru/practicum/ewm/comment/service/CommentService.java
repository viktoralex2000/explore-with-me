package ru.practicum.ewm.comment.service;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.dto.NewCommentDto;
import ru.practicum.ewm.comment.dto.RejectCommentRequest;
import ru.practicum.ewm.comment.dto.UpdateCommentDto;

import java.util.List;

public interface CommentService {

    CommentDto addComment(Long userId, Long eventId, NewCommentDto dto);

    CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto);

    void deleteComment(Long userId, Long commentId);

    void deleteCommentByAdmin(Long commentId);

    CommentDto approveComment(Long commentId);

    CommentDto rejectComment(Long commentId, RejectCommentRequest request);

    List<CommentDto> getCommentsByEvent(Long eventId, int from, int size);

    List<CommentDto> getCommentsForModeration(String state, int from, int size);
}