package com.example.minip.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.example.minip.business.ReferenceDataService;
import com.example.minip.catalog.ProductDescriptionGeneration;
import com.example.minip.catalog.ProductDescriptionGenerationRepository;
import com.example.minip.catalog.ProductDescriptionRequest;
import com.example.minip.catalog.ProductDescriptionService;
import com.example.minip.catalog.ai.ProductDescriptionProvider;
import com.example.minip.recommendation.CustomerRecommendationRequest;
import com.example.minip.recommendation.RecommendationJob;
import com.example.minip.recommendation.RecommendationRepository;
import com.example.minip.recommendation.RecommendationService;
import com.example.minip.recommendation.ai.AiRecommendationProvider;
import com.example.minip.recommendation.ai.AiRecommendationResult;
import com.example.minip.wardrobe.WardrobeItem;
import com.example.minip.wardrobe.WardrobeItemRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AiFailurePersistenceTest {
    @Autowired RecommendationService recommendationService;
    @Autowired RecommendationRepository recommendationRequests;
    @Autowired WardrobeItemRepository wardrobeItems;
    @Autowired ProductDescriptionService descriptionService;
    @Autowired ProductDescriptionGenerationRepository generations;
    @Autowired ReferenceDataService references;

    @MockBean AiRecommendationProvider recommendationProvider;
    @MockBean ProductDescriptionProvider descriptionProvider;

    @BeforeEach
    void resetProviders() {
        reset(recommendationProvider, descriptionProvider);
    }

    @Test
    void rejectsVariantOutsideClosedCandidateSetAndMarksRequestFailed() {
        long userId = 8401L;
        WardrobeItem wardrobe = wardrobeItems.save(wardrobe(userId));
        when(recommendationProvider.recommend(any())).thenReturn(new AiRecommendationResult(
            AiRecommendationResult.Status.COMPLETED,
            List.of(new AiRecommendationResult.Outfit(
                1, "Invalid look", 85, "Invalid candidate test",
                List.of(wardrobe.getId()), List.of(Long.MAX_VALUE))),
            List.of()
        ));

        assertThatThrownBy(() -> recommendationService.recommend(userId,
            recommendationRequest(wardrobe.getId())))
            .isInstanceOf(AiIntegrationException.class)
            .extracting(exception -> ((AiIntegrationException) exception).getReason())
            .isEqualTo(AiIntegrationException.Reason.INVALID_RESPONSE);

        assertThat(latestRequest(userId).getStatus()).isEqualTo(RecommendationJob.Status.FAILED);
    }

    @Test
    void marksRecommendationRequestFailedWhenProviderFails() {
        long userId = 8402L;
        WardrobeItem wardrobe = wardrobeItems.save(wardrobe(userId));
        when(recommendationProvider.recommend(any())).thenThrow(
            new AiIntegrationException(AiIntegrationException.Reason.UPSTREAM_FAILURE));

        assertThatThrownBy(() -> recommendationService.recommend(userId,
            recommendationRequest(wardrobe.getId())))
            .isInstanceOf(AiIntegrationException.class);

        assertThat(latestRequest(userId).getStatus()).isEqualTo(RecommendationJob.Status.FAILED);
    }

    @Test
    void marksDescriptionGenerationFailedWhenProviderFails() {
        long businessId = 8403L;
        ReferenceDataService.BusinessContext context = references.ensureBusiness(
            businessId, new BigDecimal("0.7000"));
        when(descriptionProvider.generate(any())).thenThrow(
            new AiIntegrationException(AiIntegrationException.Reason.UPSTREAM_FAILURE));

        assertThatThrownBy(() -> descriptionService.generate(businessId,
            descriptionRequest(businessId, context.member().getId())))
            .isInstanceOf(AiIntegrationException.class);

        ProductDescriptionGeneration generation = generations
            .findFirstByBusinessIdOrderByCreatedAtDesc(businessId).orElseThrow();
        assertThat(generation.getStatus()).isEqualTo(ProductDescriptionGeneration.Status.FAILED);
        assertThat(generation.getGeneratedDescription()).isNull();
    }

    private RecommendationJob latestRequest(long userId) {
        return recommendationRequests.findAllByUserIdOrderByCreatedAtDesc(userId).getFirst();
    }

    private CustomerRecommendationRequest recommendationRequest(long wardrobeId) {
        return new CustomerRecommendationRequest(
            "INTERVIEW", null, "Formal", "M", 30_000L, List.of(wardrobeId));
    }

    private WardrobeItem wardrobe(long userId) {
        return new WardrobeItem(userId, "Black Slacks", "BOTTOM", "BLACK", "ALL", "",
            "wardrobe.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }

    private ProductDescriptionRequest descriptionRequest(long businessId, long memberId) {
        return new ProductDescriptionRequest(
            businessId,
            memberId,
            null,
            "https://image.test/navy-blazer.jpg",
            new ProductDescriptionRequest.ProductInput(
                "Navy Single Blazer", "FITLY", "OUTER", "싱글 브레스트"),
            new VisionFeatures("OUTER", "BLAZER", "NAVY", "REGULAR",
                "STRAIGHT", "SOLID", "FORMAL", 0.93)
        );
    }
}
