package ru.practicum.ewm.comment.mapper;

import ru.practicum.ewm.comment.dto.CommentDto;
import ru.practicum.ewm.comment.model.Comment;
import ru.practicum.ewm.user.mapper.UserMapper;

public class CommentMapper {

    public static CommentDto toCommentDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthor(UserMapper.toUserDto(comment.getAuthor()));
        dto.setEventId(comment.getEvent().getId());
        dto.setCreated(comment.getCreated());
        dto.setUpdated(comment.getUpdated());
        dto.setState(comment.getState().name());
        dto.setRejectionReason(comment.getRejectionReason());
        return dto;
    }
}