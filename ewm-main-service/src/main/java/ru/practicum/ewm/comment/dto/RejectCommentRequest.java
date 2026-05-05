package ru.practicum.ewm.comment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectCommentRequest {

    @NotBlank(message = "Rejection reason cannot be empty")
    private String rejectionReason;
}