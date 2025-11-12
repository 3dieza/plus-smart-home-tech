package ru.practicum.delivery.service;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.UUID;
import ru.practicum.interaction.api.dto.delivery.DeliveryDto;
import ru.practicum.interaction.api.dto.order.OrderDto;

public interface DeliveryService {
    DeliveryDto createDelivery(@Valid DeliveryDto newDeliveryDto);

    void emulateSuccessfulDelivery(UUID orderId);

    void emulateItemPickup(UUID orderId);

    void emulateDeliveryDeclined(UUID orderId);

    BigDecimal calculateOrderDeliveryCost(@Valid OrderDto orderDto);
}
