package br.com.olivaras.api.address.dto;

import br.com.olivaras.api.address.entity.AddressEntity;
import java.time.Instant;
import java.util.UUID;

public record AddressResponse(UUID id, String label, String recipient, String zipCode,
        String street, String number, String complement, String district, String city,
        String state, String reference, boolean defaultAddress, Instant createdAt, Instant updatedAt) {
    public static AddressResponse from(AddressEntity address) {
        return new AddressResponse(address.getId(), address.getLabel(), address.getRecipient(),
                address.getZipCode(), address.getStreet(), address.getNumber(), address.getComplement(),
                address.getDistrict(), address.getCity(), address.getState(), address.getReference(),
                address.isDefaultAddress(), address.getCreatedAt(), address.getUpdatedAt());
    }
}
