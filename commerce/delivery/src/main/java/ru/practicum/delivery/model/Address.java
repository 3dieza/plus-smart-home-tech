package ru.practicum.delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UuidGenerator;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Address {
    @Id
    @UuidGenerator
    UUID addressId;

    @Column(length = 20)
    String country;

    @Column(length = 30)
    String city;

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Улица обязательна")
    String street;

    @Column(length = 10)
    String house;

    @Column(length = 10)
    String flat;
}
