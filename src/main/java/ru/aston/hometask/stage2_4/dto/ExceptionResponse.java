package ru.aston.hometask.stage2_4.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ExceptionResponse(
        String message,
        LocalDateTime timestamp,
        Map<String, String> errors
) {}