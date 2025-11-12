package ru.practicum.shopping.cart.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.interaction.api.enums.cart.CartState;
import ru.practicum.shopping.cart.model.Cart;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByUsernameAndStatus(String username, CartState status);

    Optional<Cart> findByUsername(String username);
}
