package ru.practicum.ewm.event.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateEventUserRequest {

    private String annotation;

    private Long category;

    private String description;

    private String eventDate;

    private LocationDto location;

    private Boolean paid;

    private Integer participantLimit;

    private Boolean requestModeration;

    private String title;

    private String stateAction;
}