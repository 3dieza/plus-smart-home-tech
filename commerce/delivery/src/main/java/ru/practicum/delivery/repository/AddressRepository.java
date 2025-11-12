package ru.practicum.delivery.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.delivery.model.Address;

public interface AddressRepository extends JpaRepository<Address, UUID> {
}
