package com.store.vitrine3d.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.store.vitrine3d.domain.service.MakerWorldScraperService;
import com.store.vitrine3d.domain.service.ProductService;
import com.store.vitrine3d.rest.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Tag(name = "Produtos", description = "Gerenciamento de produtos da vitrine")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final MakerWorldScraperService scraperService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public ProductController(ProductService productService,
                             MakerWorldScraperService scraperService,
                             ObjectMapper objectMapper,
                             Validator validator) {
        this.productService = productService;
        this.scraperService = scraperService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Operation(summary = "Cadastra um produto com imagem (multipart: 'data' JSON + 'image' arquivo)")
    @RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = ProductCreateMultipartDoc.class)))
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> create(
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProductCreateRequest request = objectMapper.readValue(dataJson, ProductCreateRequest.class);
        validate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProductResponse.from(productService.save(request, image)));
    }

    @Operation(summary = "Atualiza um produto (multipart: 'data' JSON + 'image' opcional)")
    @RequestBody(required = true, content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
            schema = @Schema(implementation = ProductUpdateMultipartDoc.class)))
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @RequestPart("data") String dataJson,
            @RequestPart(value = "image", required = false) MultipartFile image) throws IOException {

        ProductUpdateRequest request = objectMapper.readValue(dataJson, ProductUpdateRequest.class);
        validate(request);
        return ResponseEntity.ok(ProductResponse.from(productService.update(id, request, image)));
    }

    @Operation(summary = "Toggle de visibilidade do produto")
    @PatchMapping("/{id}/visibility")
    public ResponseEntity<ProductResponse> toggleVisibility(@PathVariable Long id) {
        return ResponseEntity.ok(ProductResponse.from(productService.toggleVisibility(id)));
    }

    @Operation(summary = "Toggle de destaque do produto (máx. 3 por loja)")
    @PatchMapping("/{id}/featured")
    public ResponseEntity<ProductResponse> toggleFeatured(@PathVariable Long id) {
        return ResponseEntity.ok(ProductResponse.from(productService.toggleFeatured(id)));
    }

    @Operation(summary = "Registra clique no botão de WhatsApp e retorna total de cliques")
    @PostMapping("/{id}/whatsapp-click")
    public ResponseEntity<Long> registerWhatsappClick(@PathVariable Long id) {
        return ResponseEntity.ok(productService.registerWhatsappClick(id));
    }

    @Operation(summary = "Faz scraping de uma URL do MakerWorld e retorna dados do modelo")
    @PostMapping("/scrape")
    public ResponseEntity<MakerWorldScrapedDataDTO> scrape(@Valid @RequestBody ScrapeRequest request) {
        return ResponseEntity.ok(scraperService.scrape(request.getUrl()));
    }

    @Operation(summary = "Lista produtos visíveis de uma loja — vitrine pública (paginado, 15/página)")
    @GetMapping("/store/{storeId}/public")
    public PageResponse<ProductResponse> listPublic(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return PageResponse.from(
                productService.findVisibleByStoreId(storeId, page, size),
                ProductResponse::from);
    }

    @Operation(summary = "Lista produtos em destaque de uma loja (vitrine pública)")
    @GetMapping("/store/{storeId}/featured")
    public List<ProductResponse> listFeatured(@PathVariable UUID storeId) {
        return productService.findFeaturedByStoreId(storeId).stream()
                .map(ProductResponse::from)
                .toList();
    }

    @Operation(summary = "Lista todos os produtos de uma loja — painel do lojista (paginado, 15/página)")
    @GetMapping("/store/{storeId}")
    public PageResponse<ProductResponse> listAll(
            @PathVariable UUID storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size) {
        return PageResponse.from(
                productService.findByStoreId(storeId, page, size),
                ProductResponse::from);
    }

    @Operation(summary = "Busca produto por ID (inclui total de cliques no WhatsApp)")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        var product = productService.findById(id);
        return ResponseEntity.ok(ProductResponse.from(product, productService.getClickCount(id)));
    }

    @Operation(summary = "Remove um produto por ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private <T> void validate(T request) {
        Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    @Schema(description = "Multipart para criação de produto")
    static class ProductCreateMultipartDoc {
        @Schema(description = "Dados do produto")
        public ProductCreateRequest data;
        @Schema(type = "string", format = "binary", description = "Imagem do produto (opcional)")
        public org.springframework.web.multipart.MultipartFile image;
    }

    @Schema(description = "Multipart para atualização de produto")
    static class ProductUpdateMultipartDoc {
        @Schema(description = "Campos a atualizar")
        public ProductUpdateRequest data;
        @Schema(type = "string", format = "binary", description = "Nova imagem (opcional)")
        public org.springframework.web.multipart.MultipartFile image;
    }
}
