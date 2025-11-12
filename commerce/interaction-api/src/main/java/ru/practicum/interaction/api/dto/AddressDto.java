package ru.practicum.interaction.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
public class AddressDto {
    UUID addressId;

    @Size(max = 20)
    String country;

    @Size(max = 30)
    String city;

    @NotBlank
    @Size(max = 50)
    String street;

    @Size(max = 10)
    String house;

    @Size(max = 10)
    String flat;
}
