package br.com.olivaras.api.address.service;

import br.com.olivaras.api.address.dto.AddressRequest;
import br.com.olivaras.api.address.dto.AddressResponse;
import br.com.olivaras.api.address.entity.AddressEntity;
import br.com.olivaras.api.address.repository.AddressRepository;
import br.com.olivaras.api.auth.entity.UserEntity;
import br.com.olivaras.api.auth.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AddressService {
    private final AddressRepository addresses;
    private final UserRepository users;

    public AddressService(AddressRepository addresses, UserRepository users) {
        this.addresses = addresses;
        this.users = users;
    }

    @Transactional(readOnly = true)
    public List<AddressResponse> list(UUID userId) {
        requireActiveUser(userId);
        return addresses.findAllByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse create(UUID userId, AddressRequest request) {
        UserEntity user = requireActiveUser(userId);
        boolean makeDefault = Boolean.TRUE.equals(request.defaultAddress())
                || !addresses.existsByUser_IdAndDefaultAddressTrue(userId);
        if (makeDefault) addresses.clearDefaultForUser(userId);
        return AddressResponse.from(addresses.saveAndFlush(new AddressEntity(user, request, makeDefault)));
    }

    @Transactional
    public AddressResponse replace(UUID userId, UUID addressId, AddressRequest request) {
        AddressEntity address = ownedAddress(userId, addressId);
        boolean makeDefault = request.defaultAddress() == null
                ? address.isDefaultAddress() : request.defaultAddress();
        if (makeDefault) addresses.clearDefaultForUser(userId);
        address.update(request, makeDefault);
        return AddressResponse.from(addresses.saveAndFlush(address));
    }

    @Transactional
    public void delete(UUID userId, UUID addressId) {
        AddressEntity address = ownedAddress(userId, addressId);
        boolean wasDefault = address.isDefaultAddress();
        addresses.delete(address);
        addresses.flush();
        if (wasDefault) {
            addresses.findFirstByUser_IdOrderByCreatedAtAsc(userId).ifPresent(next -> {
                addresses.clearDefaultForUser(userId);
                next.setDefaultAddress(true);
                addresses.save(next);
            });
        }
    }

    private UserEntity requireActiveUser(UUID userId) {
        return users.findByIdAndActiveTrue(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Perfil não encontrado"));
    }

    private AddressEntity ownedAddress(UUID userId, UUID addressId) {
        requireActiveUser(userId);
        return addresses.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Endereço não encontrado"));
    }
}
