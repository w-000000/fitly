package com.example.minip.catalog;

import com.example.minip.business.ReferenceDataService;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final RoleGuard roles;
    private final ReferenceDataService references;
    private final ProductDescriptionService descriptionService;
    public ProductController(ProductRepository products, ProductVariantRepository variants, RoleGuard roles,
                             ReferenceDataService references,
                             ProductDescriptionService descriptionService) {
        this.products = products; this.variants = variants; this.roles = roles;
        this.references = references; this.descriptionService = descriptionService;
    }

    @GetMapping
    public List<ProductView> list() { return products.findAll().stream().map(this::view).toList(); }

    @GetMapping("/{productId}")
    public ProductView get(@PathVariable Long productId) {
        return view(products.findById(productId).orElseThrow(() -> notFound("상품")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public ProductView create(@RequestHeader("X-Actor-Role") String role, @Valid @RequestBody Product.CreateRequest request) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        ReferenceDataService.BusinessContext context = references.ensureBusiness(
            request.partnerId(), request.settlementRate()
        );
        return view(products.save(new Product(context.business(), context.member(), request.name(), request.brand(),
            request.category(), request.description(), request.retailPrice(), request.rentalPrice(), request.imageUrl())));
    }

    @PatchMapping("/{productId}")
    @Transactional
    public ProductView update(@RequestHeader("X-Actor-Role") String role, @PathVariable Long productId,
                              @Valid @RequestBody UpdateProductRequest request) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        Product product = products.findById(productId).orElseThrow(() -> notFound("상품"));
        product.updateDetails(request.name(), request.brand(), request.category(), request.description(),
            request.retailPrice(), request.rentalPrice(), request.imageUrl());
        return view(product);
    }

    @PostMapping("/ai-description")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDescriptionResponse generateDescription(
        @RequestHeader("X-Actor-Role") String role,
        @RequestHeader("X-User-Id") Long userId,
        @Valid @RequestBody ProductDescriptionRequest request
    ) {
        roles.require(role, ActorRole.ROLE_PARTNER);
        return descriptionService.generate(userId, request);
    }

    @PostMapping("/{productId}/variants")
    @ResponseStatus(HttpStatus.CREATED)
    public VariantView addVariant(@RequestHeader("X-Actor-Role") String role, @PathVariable Long productId, @Valid @RequestBody VariantRequest request) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        Product product = products.findById(productId).orElseThrow(() -> notFound("상품"));
        return VariantView.from(variants.save(new ProductVariant(product, request.size(), request.stock())));
    }

    @PatchMapping("/variants/{variantId}/stock")
    @Transactional
    public VariantView updateStock(@RequestHeader("X-Actor-Role") String role, @PathVariable Long variantId, @Valid @RequestBody StockRequest request) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        ProductVariant variant = variants.findById(variantId).orElseThrow(() -> notFound("상품 옵션"));
        variant.adjustInventory(request.delta());
        return VariantView.from(variant);
    }

    @GetMapping("/variants/{variantId}/availability")
    public AvailabilityView availability(@PathVariable Long variantId, @RequestParam LocalDate startDate,
                                         @RequestParam LocalDate endDate, @RequestParam(defaultValue = "1") @Min(1) int quantity) {
        if (endDate.isBefore(startDate)) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "대여 종료일은 시작일과 같거나 이후여야 합니다.");
        ProductVariant variant = variants.findById(variantId).orElseThrow(() -> notFound("상품 옵션"));
        int remaining = variant.getAvailableStock();
        return new AvailabilityView(variantId, startDate, endDate, quantity, remaining, remaining >= quantity);
    }

    private ProductView view(Product product) {
        return new ProductView(product, variants.findAllByProductId(product.getId()).stream().map(VariantView::from).toList());
    }
    private ResponseStatusException notFound(String target) { return new ResponseStatusException(HttpStatus.NOT_FOUND, target + "을 찾을 수 없습니다."); }
    public record VariantRequest(@NotBlank String size, @Min(0) int stock) {}
    public record StockRequest(int delta) {}
    public record UpdateProductRequest(String name, String brand, String category, String description,
                                       @Positive BigDecimal retailPrice, @Positive BigDecimal rentalPrice,
                                       String imageUrl) {}
    public record ProductView(Product product, List<VariantView> variants) {}
    public record VariantView(Long id, String size, int availableStock) {
        static VariantView from(ProductVariant value) { return new VariantView(value.getId(), value.getSizeName(), value.getAvailableStock()); }
    }
    public record AvailabilityView(Long variantId, LocalDate startDate, LocalDate endDate,
                                   int requestedQuantity, int availableQuantity, boolean available) {}
}
