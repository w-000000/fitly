package com.example.minip.catalog;

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
import com.example.minip.rental.RentalOrder;
import com.example.minip.rental.RentalOrderRepository;
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
    private final RentalOrderRepository rentals;
    public ProductController(ProductRepository products, ProductVariantRepository variants, RoleGuard roles, RentalOrderRepository rentals) {
        this.products = products; this.variants = variants; this.roles = roles; this.rentals = rentals;
    }

    @GetMapping
    public List<ProductView> list(@RequestParam(required = false) Long partnerId,
                                  @RequestParam(required = false) Product.Status status,
                                  @RequestParam(required = false) String category,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(defaultValue = "0") @Min(0) int page,
                                  @RequestParam(defaultValue = "20") @Min(1) int size) {
        return products.findAll().stream()
            .filter(value -> partnerId == null || partnerId.equals(value.getPartnerId()))
            .filter(value -> status == null || status == value.getStatus())
            .filter(value -> category == null || category.equalsIgnoreCase(value.getCategory()))
            .filter(value -> q == null || contains(value.getName(), q) || contains(value.getBrand(), q))
            .skip((long) page * Math.min(size, 100)).limit(Math.min(size, 100))
            .map(this::view).toList();
    }

    @GetMapping("/{productId}")
    public ProductView get(@PathVariable Long productId) {
        return view(products.findById(productId).orElseThrow(() -> notFound("상품")));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductView create(@RequestHeader("X-Actor-Role") String role, @Valid @RequestBody Product.CreateRequest request) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        return view(products.save(new Product(request.partnerId(), request.name(), request.brand(), request.category(),
            request.description(), request.retailPrice(), request.rentalPrice(), request.settlementRate(), request.imageUrl())));
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
    public AiDescription generateDescription(@RequestHeader("X-Actor-Role") String role,
                                             @Valid @RequestBody AiDescriptionRequest request) {
        roles.require(role, ActorRole.ROLE_PARTNER, ActorRole.ROLE_ADMIN);
        String brand = request.brand() == null || request.brand().isBlank() ? "브랜드" : request.brand();
        String description = "%s의 %s 상품입니다. 깔끔한 디자인으로 다양한 상황에 활용하기 좋으며, 보유 의류와 자연스럽게 조합할 수 있습니다."
            .formatted(brand, request.name());
        return new AiDescription(description, true);
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
        int reserved = rentals.findAllByVariantIdAndStatusInAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            variantId, List.of(RentalOrder.Status.RENTED, RentalOrder.Status.RETURN_REQUESTED), endDate, startDate
        ).stream().mapToInt(RentalOrder::getQuantity).sum();
        int remaining = Math.max(0, variant.getTotalStock() - reserved);
        return new AvailabilityView(variantId, startDate, endDate, quantity, remaining, remaining >= quantity);
    }

    private ProductView view(Product product) {
        return new ProductView(product, variants.findAllByProductId(product.getId()).stream().map(VariantView::from).toList());
    }
    private boolean contains(String value, String query) {
        return value != null && value.toLowerCase().contains(query.toLowerCase());
    }
    private ResponseStatusException notFound(String target) { return new ResponseStatusException(HttpStatus.NOT_FOUND, target + "을 찾을 수 없습니다."); }
    public record VariantRequest(@NotBlank String size, @Min(0) int stock) {}
    public record StockRequest(int delta) {}
    public record UpdateProductRequest(String name, String brand, String category, String description,
                                       @Positive BigDecimal retailPrice, @Positive BigDecimal rentalPrice,
                                       String imageUrl) {}
    public record AiDescriptionRequest(@NotBlank String name, String brand, @NotBlank String category,
                                       String imageUrl) {}
    public record AiDescription(String description, boolean mock) {}
    public record ProductView(Product product, List<VariantView> variants) {}
    public record VariantView(Long id, String size, int availableStock) {
        static VariantView from(ProductVariant value) { return new VariantView(value.getId(), value.getSizeName(), value.getAvailableStock()); }
    }
    public record AvailabilityView(Long variantId, LocalDate startDate, LocalDate endDate,
                                   int requestedQuantity, int availableQuantity, boolean available) {}
}
