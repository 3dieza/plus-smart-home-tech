package ru.yandex.practicum.telemetry.collector.dto;

import java.util.List;

public record ErrorResponse(
        String code,
        String message,
        List<String> details
) {}