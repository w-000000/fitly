package com.example.minip.recommendation;

import com.example.minip.catalog.Product;
import com.example.minip.catalog.ProductRepository;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationRepository recommendations;
    private final ProductRepository products;
    private final RoleGuard roles;
    public RecommendationController(RecommendationRepository recommendations, ProductRepository products, RoleGuard roles) {
        this.recommendations = recommendations; this.products = products; this.roles = roles;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result create(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @Valid @RequestBody CreateRequest request) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        RecommendationRequest saved = recommendations.save(new RecommendationRequest(userId, request.tpo(), request.preferredStyle(), request.wardrobeDescription(), request.wardrobeImageUrl()));
        return result(saved);
    }
    @GetMapping("/{id}")
    public Result get(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER, ActorRole.ROLE_ADMIN);
        RecommendationRequest value = recommendations.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 요청을 찾을 수 없습니다."));
        if (ActorRole.ROLE_CUSTOMER.name().equals(role) && !value.getCustomerId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 추천만 조회할 수 있습니다.");
        return result(value);
    }
    private Result result(RecommendationRequest request) { return new Result(request, products.findAll().stream().limit(2).toList(), true); }
    public record CreateRequest(@NotBlank String tpo, @NotBlank String preferredStyle, @NotNull String wardrobeDescription, String wardrobeImageUrl) {}
    public record Result(RecommendationRequest request, List<Product> recommendedProducts, boolean mock) {}
}
