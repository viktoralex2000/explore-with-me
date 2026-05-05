package ru.practicum.ewm.event.dto;

import lombok.Data;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;
    private String status;
}