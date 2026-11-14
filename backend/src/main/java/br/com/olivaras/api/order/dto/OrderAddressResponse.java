package br.com.olivaras.api.order.dto;

import br.com.olivaras.api.order.entity.StoreOrderEntity;

public record OrderAddressResponse(String recipient, String zipCode, String street, String number,
                                   String complement, String district, String city, String state,
                                   String reference) {
    public static OrderAddressResponse from(StoreOrderEntity order) {
        return new OrderAddressResponse(order.getRecipient(), order.getZipCode(), order.getStreet(),
                order.getStreetNumber(), order.getComplement(), order.getDistrict(), order.getCity(),
                order.getState(), order.getDeliveryReference());
    }
}
