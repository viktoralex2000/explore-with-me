package ru.practicum.ewm.comment.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import ru.practicum.ewm.user.dto.UserDto;

import java.time.LocalDateTime;

@Data
public class CommentDto {

    private Long id;
    private String text;
    private UserDto author;
    private Long eventId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime created;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated;

    private String state;
    private String rejectionReason;
}