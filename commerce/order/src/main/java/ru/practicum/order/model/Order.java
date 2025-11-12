package ru.practicum.order.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;
import ru.practicum.interaction.api.enums.order.OrderState;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Order {
    @Id
    @UuidGenerator
    UUID orderId;

    @Column(nullable = false)
    UUID shoppingCartId;

    @ElementCollection
    @CollectionTable(name = "order_product", joinColumns = @JoinColumn(name = "order_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    @Builder.Default
    Map<UUID, Long> products = new HashMap<>();

    UUID paymentId;

    UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    OrderState state = OrderState.NEW;

    @Column(precision = 10, scale = 3)
    BigDecimal deliveryWeight;

    @Column(precision = 10, scale = 3)
    BigDecimal deliveryVolume;

    Boolean fragile;

    @Column(precision = 10, scale = 2)
    BigDecimal totalPrice;

    @Column(precision = 10, scale = 2)
    BigDecimal deliveryPrice;

    @Column(precision = 10, scale = 2)
    BigDecimal productPrice;

    @Column(nullable = false, unique = true, length = 50)
    String username;
}
