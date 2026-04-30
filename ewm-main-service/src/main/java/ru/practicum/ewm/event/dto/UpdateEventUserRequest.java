package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
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

    @PositiveOrZero(message = "Participant limit must be zero or positive")
    private Integer participantLimit;

    private Boolean requestModeration;

    private String title;

    private String stateAction;
}