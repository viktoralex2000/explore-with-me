package ru.practicum.ewm.comment.dto;

import lombok.Getter;
import lombok.Setter;
import ru.practicum.ewm.user.dto.UserDto;

@Getter
@Setter
public class CommentDto {

    private Long id;
    private String text;
    private UserDto author;
    private Long eventId;
    private String created;
    private String updated;
    private String state;
    private String rejectionReason;
}