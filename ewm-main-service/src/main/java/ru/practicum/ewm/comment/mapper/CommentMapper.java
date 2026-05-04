package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.user.mapper.UserMapper;

import java.time.format.DateTimeFormatter;

public class CommentMapper {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(UserMapper.toUserDto(comment.getAuthor()));
        dto.setEventId(comment.getEvent().getId());
        dto.setCreated(comment.getCreated() != null ? comment.getCreated().format(FORMATTER) : null);
        dto.setUpdated(comment.getUpdated() != null ? comment.getUpdated().format(FORMATTER) : null);
        dto.setState(comment.getState().name());
        dto.setRejectionReason(comment.getRejectionReason());
        return dto;
    }
}