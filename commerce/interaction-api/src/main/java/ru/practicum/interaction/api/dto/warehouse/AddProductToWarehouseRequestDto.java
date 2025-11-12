package ru.practicum.interaction.api.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AddProductToWarehouseRequestDto {
    @NotNull
    UUID productId;

    @NotNull(message = "Необходимо указать количество")
    @Min(value = 1, message = "Значение должно быть не менее 1")
    Long quantity;
}
