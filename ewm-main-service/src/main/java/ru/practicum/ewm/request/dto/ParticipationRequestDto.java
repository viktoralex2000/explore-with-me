package ru.practicum.ewm.request.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipationRequestDto {

    private Long id;
    private String created;
    private Long event;
    private Long requester;
    private String status;
}