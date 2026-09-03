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
    private final RecommendationService service;
    private final RecommendationRepository recommendations;
    private final ProductRepository products;
    private final RoleGuard roles;
    public RecommendationController(RecommendationService service, RecommendationRepository recommendations,
                                    ProductRepository products, RoleGuard roles) {
        this.service = service; this.recommendations = recommendations; this.products = products; this.roles = roles;
    }
    @PostMapping
    public RecommendationResponse recommend(@Valid @RequestBody RecommendationRequest request) { return service.recommend(request); }
    @PostMapping("/jobs") @ResponseStatus(HttpStatus.CREATED)
    public Result createJob(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId,
                            @Valid @RequestBody CreateJobRequest request) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        RecommendationJob saved = recommendations.save(new RecommendationJob(userId, request.tpo(), request.preferredStyle(),
            request.wardrobeDescription(), request.wardrobeImageUrl()));
        return result(saved);
    }
    @GetMapping("/jobs/{id}")
    public Result getJob(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId, @PathVariable Long id) {
        roles.require(role, ActorRole.ROLE_CUSTOMER, ActorRole.ROLE_ADMIN);
        RecommendationJob value = recommendations.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 요청을 찾을 수 없습니다."));
        if (ActorRole.ROLE_CUSTOMER.name().equals(role) && !value.getCustomerId().equals(userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 추천만 조회할 수 있습니다.");
        return result(value);
    }
    private Result result(RecommendationJob request) { return new Result(request, products.findAll().stream().limit(2).toList(), true); }
    public record CreateJobRequest(@NotBlank String tpo, @NotBlank String preferredStyle, @NotNull String wardrobeDescription, String wardrobeImageUrl) {}
    public record Result(RecommendationJob request, List<Product> recommendedProducts, boolean mock) {}
}
