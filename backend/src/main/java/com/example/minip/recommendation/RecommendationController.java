package com.example.minip.recommendation;

import com.example.minip.business.ReferenceDataService;
import com.example.minip.catalog.Product;
import com.example.minip.catalog.ProductRepository;
import com.example.minip.config.ActorRole;
import com.example.minip.config.RoleGuard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService service;
    private final RuleRecommendationService ruleService;
    private final RecommendationRepository recommendations;
    private final ProductRepository products;
    private final RoleGuard roles;
    private final SavedOutfitRepository savedOutfits;
    private final StyleRepository styles;
    private final RecommendationResultRepository results;
    private final ReferenceDataService references;
    public RecommendationController(RecommendationService service, RuleRecommendationService ruleService,
                                    RecommendationRepository recommendations, ProductRepository products,
                                    RoleGuard roles, SavedOutfitRepository savedOutfits, StyleRepository styles,
                                    RecommendationResultRepository results, ReferenceDataService references) {
        this.service = service; this.ruleService = ruleService; this.recommendations = recommendations;
        this.products = products; this.roles = roles;
        this.savedOutfits = savedOutfits; this.styles = styles; this.results = results; this.references = references;
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerRecommendationResponse recommend(@RequestHeader("X-Actor-Role") String role,
                                                      @RequestHeader("X-User-Id") Long userId,
                                                      @Valid @RequestBody CustomerRecommendationRequest request) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        return service.recommend(userId, request);
    }
    @PostMapping("/rules")
    public RecommendationResponse recommendByRules(@Valid @RequestBody RecommendationRequest request) {
        return ruleService.recommend(request);
    }
    @PostMapping("/jobs") @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Result createJob(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId,
                            @Valid @RequestBody CreateJobRequest request) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        references.ensureUser(userId, ActorRole.ROLE_CUSTOMER);
        String styleName = normalizeStyle(request.preferredStyle());
        Style style = styles.findByName(styleName).orElseGet(() -> styles.save(new Style(styleName)));
        RecommendationJob saved = recommendations.save(new RecommendationJob(userId, request.tpo(), style,
            request.wardrobeDescription(), request.wardrobeImageUrl()));
        String comment = request.tpo() + " 상황에 맞춰 " + styleName + " 무드의 균형 잡힌 조합을 추천합니다.";
        results.save(new RecommendationResult(saved, comment));
        saved.complete();
        saved.setStylingComment(comment);
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
    @GetMapping("/saved")
    public List<SavedOutfit> saved(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId){
        roles.require(role,ActorRole.ROLE_CUSTOMER);return savedOutfits.findAllForUser(userId);
    }
    @PutMapping("/jobs/{jobId}/looks/{lookKey}/saved")
    @Transactional
    public SavedState save(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId,
                           @PathVariable Long jobId,@PathVariable String lookKey,@Valid @RequestBody SaveRequest request){
        roles.require(role,ActorRole.ROLE_CUSTOMER);ownJob(jobId,userId);
        RecommendationResult recommendation = results.findFirstByRequestIdOrderByRank(jobId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "추천 결과를 찾을 수 없습니다."));
        SavedOutfit value=savedOutfits.findSaved(userId,jobId,lookKey)
            .orElseGet(()->savedOutfits.save(new SavedOutfit(recommendation,lookKey,request.title(),request.description())));
        return new SavedState(true,value);
    }
    @DeleteMapping("/jobs/{jobId}/looks/{lookKey}/saved")
    @Transactional
    public SavedState unsave(@RequestHeader("X-Actor-Role") String role,@RequestHeader("X-User-Id") Long userId,
                             @PathVariable Long jobId,@PathVariable String lookKey){
        roles.require(role,ActorRole.ROLE_CUSTOMER);ownJob(jobId,userId);
        savedOutfits.findSaved(userId,jobId,lookKey).ifPresent(savedOutfits::delete);
        return new SavedState(false,null);
    }
    private RecommendationJob ownJob(Long id,Long userId){RecommendationJob value=recommendations.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"추천 요청을 찾을 수 없습니다."));if(!value.getCustomerId().equals(userId))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"본인의 추천만 관리할 수 있습니다.");return value;}
    @GetMapping("/jobs")
    public List<Result> myJobs(@RequestHeader("X-Actor-Role") String role, @RequestHeader("X-User-Id") Long userId) {
        roles.require(role, ActorRole.ROLE_CUSTOMER);
        return recommendations.findAllByUserIdOrderByCreatedAtDesc(userId).stream().map(this::result).toList();
    }
    private Result result(RecommendationJob request) {
        results.findFirstByRequestIdOrderByRank(request.getId())
            .ifPresent(value -> request.setStylingComment(value.getStylingComment()));
        return new Result(request, products.findAll().stream().limit(2).toList(), true);
    }
    private String normalizeStyle(String value) {
        return switch (value == null ? "" : value.trim().toLowerCase()) {
            case "minimal", "미니멀" -> "Minimal";
            case "street", "스트릿" -> "Street";
            case "casual", "캐주얼" -> "Casual";
            default -> "Formal";
        };
    }
    public record CreateJobRequest(@NotBlank String tpo, @NotBlank String preferredStyle, @NotNull String wardrobeDescription, String wardrobeImageUrl) {}
    public record Result(RecommendationJob request, List<Product> recommendedProducts, boolean mock) {}
    public record SaveRequest(@NotBlank String title,String description){}
    public record SavedState(boolean saved,SavedOutfit outfit){}
}
