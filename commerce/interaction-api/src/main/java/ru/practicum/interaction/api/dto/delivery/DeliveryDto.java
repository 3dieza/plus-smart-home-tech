package ru.practicum.interaction.api.dto.delivery;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.validation.annotation.Validated;
import ru.practicum.interaction.api.dto.AddressDto;
import ru.practicum.interaction.api.enums.delivery.DeliveryState;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Validated
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DeliveryDto {
    UUID deliveryId;

    @NotNull
    @Valid
    AddressDto fromAddress;

    @NotNull
    @Valid
    AddressDto toAddress;

    @NotNull
    UUID orderId;

    @NotNull
    DeliveryState deliveryState;
}
