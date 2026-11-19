package br.com.olivaras.api.catalog.controller;

import br.com.olivaras.api.catalog.dto.CatalogItemResponse;
import br.com.olivaras.api.catalog.service.CatalogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/lojaolivaras/api/catalogo")
@Tag(name = "Catálogo", description = "Consulta pública de varinhas disponíveis na loja")
public class CatalogController {
    private final CatalogService catalog;

    public CatalogController(CatalogService catalog) { this.catalog = catalog; }

    @GetMapping("/list-item")
    @Operation(summary = "Lista os itens ativos do catálogo", description = "Paginação: page começa em zero; size aceita de 1 a 100.")
    public Page<CatalogItemResponse> listItems(
            @Parameter(description = "Página, iniciando em zero") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens, de 1 a 100") @RequestParam(defaultValue = "20") int size) {
        return catalog.list(page, size);
    }

    @GetMapping("/search")
    @Operation(summary = "Busca varinhas por texto, madeira ou núcleo")
    public Page<CatalogItemResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String wood,
            @RequestParam(required = false) String core,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return catalog.search(q, wood, core, page, size);
    }

    @GetMapping("/item-detail/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Consulta os detalhes de uma varinha pelo UUID")
    public CatalogItemResponse detail(@PathVariable UUID id) { return catalog.detail(id); }

    @GetMapping("/item-detail/{id}/related")
    @Operation(summary = "Lista até três varinhas com características em comum")
    public List<CatalogItemResponse> related(@PathVariable UUID id) { return catalog.related(id); }
}
