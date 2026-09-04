package com.example.minip.recommendation;

import com.example.minip.ai.AiIntegrationException;
import com.example.minip.ai.AiProperties;
import com.example.minip.ai.VisionAnalysisProvider;
import com.example.minip.business.ReferenceDataService;
import com.example.minip.catalog.ProductVariant;
import com.example.minip.catalog.ProductVariantRepository;
import com.example.minip.config.ActorRole;
import com.example.minip.recommendation.CustomerRecommendationResponse.RecommendationView;
import com.example.minip.recommendation.CustomerRecommendationResponse.RentalItemView;
import com.example.minip.recommendation.CustomerRecommendationResponse.WardrobeItemView;
import com.example.minip.recommendation.ai.AiRecommendationContext;
import com.example.minip.recommendation.ai.AiRecommendationProvider;
import com.example.minip.recommendation.ai.AiRecommendationResult;
import com.example.minip.wardrobe.WardrobeItem;
import com.example.minip.wardrobe.WardrobeItemRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecommendationService {
    private final RecommendationRepository requests;
    private final RecommendationRequestWardrobeRepository requestWardrobes;
    private final RecommendationResultRepository recommendations;
    private final RecommendationItemRepository recommendationItems;
    private final WardrobeItemRepository wardrobeItems;
    private final ProductVariantRepository productVariants;
    private final StyleRepository styles;
    private final ReferenceDataService references;
    private final AiRecommendationProvider provider;
    private final VisionAnalysisProvider visionProvider;
    private final AiProperties aiProperties;
    private final TransactionTemplate transactions;

    public RecommendationService(RecommendationRepository requests,
                                 RecommendationRequestWardrobeRepository requestWardrobes,
                                 RecommendationResultRepository recommendations,
                                 RecommendationItemRepository recommendationItems,
                                 WardrobeItemRepository wardrobeItems,
                                 ProductVariantRepository productVariants,
                                 StyleRepository styles, ReferenceDataService references,
                                 AiRecommendationProvider provider,
                                 VisionAnalysisProvider visionProvider, AiProperties aiProperties,
                                 PlatformTransactionManager transactionManager) {
        this.requests = requests;
        this.requestWardrobes = requestWardrobes;
        this.recommendations = recommendations;
        this.recommendationItems = recommendationItems;
        this.wardrobeItems = wardrobeItems;
        this.productVariants = productVariants;
        this.styles = styles;
        this.references = references;
        this.provider = provider;
        this.visionProvider = visionProvider;
        this.aiProperties = aiProperties;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public CustomerRecommendationResponse recommend(Long userId,
                                                    CustomerRecommendationRequest request) {
        PreparedRecommendation prepared = transactions.execute(status -> prepare(userId, request));
        if (prepared == null) {
            throw new IllegalStateException("추천 요청을 준비하지 못했습니다.");
        }
        if (prepared.candidates().isEmpty()) {
            return completeWithoutMatches(prepared.requestId());
        }
        try {
            AiRecommendationResult aiResult = provider.recommend(toAiContext(prepared, request));
            return transactions.execute(status -> persist(prepared, request, aiResult));
        } catch (RuntimeException exception) {
            markFailed(prepared.requestId());
            throw exception;
        }
    }

    private PreparedRecommendation prepare(Long userId, CustomerRecommendationRequest input) {
        references.ensureUser(userId, ActorRole.ROLE_CUSTOMER);
        Style style = resolveStyle(input);
        List<WardrobeItem> selectedWardrobe = resolveWardrobe(userId, input.wardrobeItemIds());
        RecommendationJob request = requests.save(
            new RecommendationJob(userId, input.tpo().trim(), style, normalizeSize(input.size()))
        );
        requestWardrobes.saveAll(selectedWardrobe.stream()
            .map(item -> new RecommendationRequestWardrobe(request, item)).toList());
        request.startProcessing();
        List<ProductVariant> candidates = productVariants.findRecommendationCandidates(
            normalizeSize(input.size()), input.budget(), PageRequest.of(0, aiProperties.getMaxCandidates())
        );
        return new PreparedRecommendation(request.getId(), style.getName(), selectedWardrobe, candidates);
    }

    private Style resolveStyle(CustomerRecommendationRequest input) {
        if (input.styleId() != null) {
            return styles.findById(input.styleId()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "선택한 스타일을 찾을 수 없습니다."));
        }
        String name = normalizeStyle(input.style());
        return styles.findByName(name).orElseGet(() -> styles.save(new Style(name)));
    }

    private List<WardrobeItem> resolveWardrobe(Long userId, List<Long> ids) {
        List<Long> uniqueIds = ids.stream().distinct().toList();
        if (uniqueIds.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "같은 옷장 항목을 중복 선택할 수 없습니다.");
        }
        List<WardrobeItem> selected = new ArrayList<>();
        for (Long id : uniqueIds) {
            WardrobeItem item = wardrobeItems.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "옷장 항목을 찾을 수 없습니다."));
            if (!item.getCustomerId().equals(userId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "본인의 옷만 추천에 사용할 수 있습니다.");
            }
            selected.add(item);
        }
        return selected;
    }

    private AiRecommendationContext toAiContext(PreparedRecommendation prepared,
                                                CustomerRecommendationRequest input) {
        List<AiRecommendationContext.WardrobeCandidate> wardrobe = prepared.wardrobe().stream()
            .map(item -> new AiRecommendationContext.WardrobeCandidate(
                item.getId(), item.getName(), visionProvider.analyze(
                    new VisionAnalysisProvider.VisionAnalysisInput(item.getId(), item.getStoredImageUrl(),
                        detectedOrCanonical(item), detectedOrColor(item)))))
            .toList();
        List<AiRecommendationContext.RentalCandidate> candidates = prepared.candidates().stream()
            .map(this::toCandidate).toList();
        return new AiRecommendationContext(input.tpo().trim(), List.of(prepared.styleName()),
            normalizeSize(input.size()), input.budget(), wardrobe, candidates);
    }

    private String detectedOrCanonical(WardrobeItem item) {
        return item.getAiDetectedCategory() == null
            ? item.getCanonicalCategory() : item.getAiDetectedCategory();
    }

    private String detectedOrColor(WardrobeItem item) {
        return item.getAiDetectedColor() == null ? item.getColor() : item.getAiDetectedColor();
    }

    private AiRecommendationContext.RentalCandidate toCandidate(ProductVariant variant) {
        return new AiRecommendationContext.RentalCandidate(
            variant.getProduct().getId(), variant.getId(), variant.getProduct().getBrand(),
            variant.getProduct().getName(), variant.getProduct().getCategory(),
            variant.getSizeName(), variant.getProduct().getRentalPrice().longValueExact(),
            variant.getAvailableStock()
        );
    }

    private CustomerRecommendationResponse persist(PreparedRecommendation prepared,
                                                   CustomerRecommendationRequest input,
                                                   AiRecommendationResult aiResult) {
        validateTopLevel(aiResult);
        RecommendationJob request = requests.findById(prepared.requestId()).orElseThrow();
        if (aiResult.status() == AiRecommendationResult.Status.NO_MATCH) {
            request.complete();
            return emptyResponse(request.getId(), AiRecommendationResult.Status.NO_MATCH, List.of());
        }
        if (aiResult.status() == AiRecommendationResult.Status.NEEDS_INPUT) {
            request.fail();
            List<String> missing = aiResult.missingFields() == null
                ? List.of() : List.copyOf(aiResult.missingFields());
            return emptyResponse(request.getId(), AiRecommendationResult.Status.NEEDS_INPUT, missing);
        }

        Map<Long, WardrobeItem> wardrobeById = byWardrobeId(prepared.wardrobe());
        Map<Long, ProductVariant> candidateById = byVariantId(prepared.candidates());
        List<AiRecommendationResult.Outfit> outfits = aiResult.recommendations();
        validateRanks(outfits);
        List<RecommendationView> views = new ArrayList<>();
        for (AiRecommendationResult.Outfit outfit : outfits) {
            views.add(persistOutfit(request, input, outfit, wardrobeById, candidateById));
        }
        request.complete();
        return new CustomerRecommendationResponse(request.getId(),
            AiRecommendationResult.Status.COMPLETED, views, List.of());
    }

    private RecommendationView persistOutfit(RecommendationJob request,
                                             CustomerRecommendationRequest input,
                                             AiRecommendationResult.Outfit outfit,
                                             Map<Long, WardrobeItem> wardrobeById,
                                             Map<Long, ProductVariant> candidateById) {
        validateOutfit(outfit, wardrobeById.keySet(), candidateById.keySet());
        BigDecimal normalizedScore = BigDecimal.valueOf(outfit.matchScore())
            .divide(BigDecimal.valueOf(100), 4, RoundingMode.UNNECESSARY);
        RecommendationResult recommendation = recommendations.save(new RecommendationResult(
            request, outfit.rank(), normalizedScore, outfit.stylingReason().trim()));

        int sortOrder = 1;
        List<WardrobeItemView> wardrobeViews = new ArrayList<>();
        for (Long wardrobeId : outfit.wardrobeItemIds()) {
            WardrobeItem item = wardrobeById.get(wardrobeId);
            recommendationItems.save(RecommendationItem.wardrobe(recommendation, item, sortOrder++));
            wardrobeViews.add(new WardrobeItemView(item.getId(), item.getName(), 0));
        }

        List<RentalItemView> rentalViews = new ArrayList<>();
        long total = 0;
        for (Long variantId : outfit.rentalProductVariantIds()) {
            ProductVariant variant = candidateById.get(variantId);
            long price = variant.getProduct().getRentalPrice().longValueExact();
            total = Math.addExact(total, price);
            recommendationItems.save(RecommendationItem.rental(
                recommendation, variant, price, sortOrder++));
            rentalViews.add(new RentalItemView(variant.getProduct().getId(), variant.getId(),
                variant.getProduct().getName(), variant.getProduct().getBrand(),
                variant.getSizeName(), price));
        }
        if (input.budget() != null && total > input.budget()) {
            throw invalidAiResponse();
        }
        return new RecommendationView(recommendation.getId(), outfit.rank(),
            outfit.outfitTitle().trim(), outfit.matchScore(), outfit.stylingReason().trim(),
            wardrobeViews, rentalViews, total);
    }

    private void validateTopLevel(AiRecommendationResult result) {
        if (result == null || result.status() == null) {
            throw invalidAiResponse();
        }
        if (result.status() == AiRecommendationResult.Status.COMPLETED
            && (result.recommendations() == null || result.recommendations().isEmpty())) {
            throw invalidAiResponse();
        }
        if (result.status() != AiRecommendationResult.Status.COMPLETED
            && result.recommendations() != null && !result.recommendations().isEmpty()) {
            throw invalidAiResponse();
        }
    }

    private void validateRanks(List<AiRecommendationResult.Outfit> outfits) {
        List<AiRecommendationResult.Outfit> ordered = outfits.stream()
            .sorted((left, right) -> Integer.compare(left.rank(), right.rank())).toList();
        for (int index = 0; index < ordered.size(); index++) {
            if (ordered.get(index).rank() != index + 1) {
                throw invalidAiResponse();
            }
        }
    }

    private void validateOutfit(AiRecommendationResult.Outfit outfit, Set<Long> wardrobeIds,
                               Set<Long> candidateIds) {
        if (outfit.matchScore() < 0 || outfit.matchScore() > 100
            || outfit.outfitTitle() == null || outfit.outfitTitle().isBlank()
            || outfit.stylingReason() == null || outfit.stylingReason().isBlank()
            || outfit.wardrobeItemIds() == null || outfit.rentalProductVariantIds() == null
            || !wardrobeIds.containsAll(outfit.wardrobeItemIds())
            || !candidateIds.containsAll(outfit.rentalProductVariantIds())
            || hasDuplicates(outfit.wardrobeItemIds())
            || hasDuplicates(outfit.rentalProductVariantIds())
            || (outfit.wardrobeItemIds().isEmpty() && outfit.rentalProductVariantIds().isEmpty())) {
            throw invalidAiResponse();
        }
    }

    private boolean hasDuplicates(List<Long> ids) {
        return new HashSet<>(ids).size() != ids.size();
    }

    private Map<Long, WardrobeItem> byWardrobeId(List<WardrobeItem> values) {
        Map<Long, WardrobeItem> result = new LinkedHashMap<>();
        values.forEach(value -> result.put(value.getId(), value));
        return result;
    }

    private Map<Long, ProductVariant> byVariantId(List<ProductVariant> values) {
        Map<Long, ProductVariant> result = new HashMap<>();
        values.forEach(value -> result.put(value.getId(), value));
        return result;
    }

    private CustomerRecommendationResponse completeWithoutMatches(Long requestId) {
        return transactions.execute(status -> {
            RecommendationJob request = requests.findById(requestId).orElseThrow();
            request.complete();
            return emptyResponse(requestId, AiRecommendationResult.Status.NO_MATCH, List.of());
        });
    }

    private CustomerRecommendationResponse emptyResponse(Long requestId,
                                                         AiRecommendationResult.Status status,
                                                         List<String> missingFields) {
        return new CustomerRecommendationResponse(requestId, status, List.of(), missingFields);
    }

    private void markFailed(Long requestId) {
        transactions.executeWithoutResult(status ->
            requests.findById(requestId).ifPresent(RecommendationJob::fail));
    }

    private String normalizeSize(String size) {
        return size.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStyle(String style) {
        if (style == null || style.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "스타일을 선택해주세요.");
        }
        return switch (style.trim().toLowerCase(Locale.ROOT)) {
            case "minimal", "미니멀" -> "Minimal";
            case "formal", "포멀" -> "Formal";
            case "street", "스트릿" -> "Street";
            case "casual", "캐주얼" -> "Casual";
            default -> throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "지원하지 않는 스타일입니다.");
        };
    }

    private AiIntegrationException invalidAiResponse() {
        return new AiIntegrationException(AiIntegrationException.Reason.INVALID_RESPONSE);
    }

    private record PreparedRecommendation(
        Long requestId,
        String styleName,
        List<WardrobeItem> wardrobe,
        List<ProductVariant> candidates
    ) {
    }
}
