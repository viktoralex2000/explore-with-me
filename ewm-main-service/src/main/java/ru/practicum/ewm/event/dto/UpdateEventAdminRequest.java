package ru.practicum.ewm.event.dto;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UpdateEventAdminRequest {

    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    private String eventDate;

    private LocationDto location;

    private Boolean paid;

    @PositiveOrZero(message = "Participant limit must be zero or positive")
    private Integer participantLimit;

    private Boolean requestModeration;

    @Size(min = 3, max = 120)
    private String title;

    private String stateAction;

    private LocalDateTime publishedOn;
}