package br.com.olivaras.api.catalog.service;

import br.com.olivaras.api.catalog.dto.CatalogItemResponse;
import br.com.olivaras.api.catalog.entity.WandProductEntity;
import br.com.olivaras.api.catalog.repository.WandProductRepository;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
    private static final int MAX_PAGE_SIZE = 100;
    private final WandProductRepository products;

    public CatalogService(WandProductRepository products) { this.products = products; }

    @Transactional(readOnly = true)
    public Page<CatalogItemResponse> list(int page, int size) {
        Pageable pageable = pageable(page, size);
        return products.findAllByActiveTrueAndCustomerOwnerIdIsNull(pageable).map(CatalogItemResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<CatalogItemResponse> search(String term, String wood, String core, int page, int size) {
        Pageable pageable = pageable(page, size);
        return products.search(normalize(term), normalize(wood), normalize(core), pageable)
                .map(CatalogItemResponse::from);
    }

    @Transactional(readOnly = true)
    public CatalogItemResponse detail(UUID id) {
        WandProductEntity product = products.findByIdAndActiveTrueAndCustomerOwnerIdIsNull(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
        return CatalogItemResponse.from(product);
    }

    @Transactional(readOnly = true)
    public List<CatalogItemResponse> related(UUID id) {
        return products.findRelated(id).stream().map(CatalogItemResponse::from).toList();
    }

    private Pageable pageable(int page, int size) {
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Use page >= 0 e size entre 1 e " + MAX_PAGE_SIZE);
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
    }

    private String normalize(String value) {
        // Sempre envia texto não nulo ao PostgreSQL. Parâmetros nulos usados
        // em expressões `:param is null` podem ser inferidos como bytea pelo driver.
        if (value == null || value.isBlank()) return "";
        return value.trim().toLowerCase(Locale.ROOT);
    }
}
