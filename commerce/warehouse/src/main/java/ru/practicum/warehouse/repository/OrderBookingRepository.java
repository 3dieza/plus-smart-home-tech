package ru.practicum.warehouse.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.warehouse.model.OrderBooking;

public interface OrderBookingRepository extends JpaRepository<OrderBooking, UUID> {
}
