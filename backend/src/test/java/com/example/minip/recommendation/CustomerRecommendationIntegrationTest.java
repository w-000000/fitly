package com.example.minip.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.minip.wardrobe.WardrobeItem;
import com.example.minip.wardrobe.WardrobeItemRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class CustomerRecommendationIntegrationTest {
    @Autowired RecommendationService service;
    @Autowired RecommendationRepository requests;
    @Autowired RecommendationResultRepository recommendations;
    @Autowired RecommendationItemRepository items;
    @Autowired RecommendationRequestWardrobeRepository requestWardrobes;
    @Autowired WardrobeItemRepository wardrobeItems;

    @Test
    void persistsNormalizedScoreWardrobeAndDatabasePriceSnapshot() {
        long userId = 8201L;
        WardrobeItem wardrobe = wardrobeItems.save(wardrobe(userId, "Black Slacks"));

        CustomerRecommendationResponse response = service.recommend(userId,
            new CustomerRecommendationRequest(
                "INTERVIEW", null, "Formal", "M", 30_000L, List.of(wardrobe.getId())));

        assertThat(response.status()).isEqualTo(
            com.example.minip.recommendation.ai.AiRecommendationResult.Status.COMPLETED);
        assertThat(response.recommendations()).hasSize(1);
        CustomerRecommendationResponse.RecommendationView view = response.recommendations().getFirst();
        assertThat(view.matchScore()).isEqualTo(85);
        assertThat(view.wardrobeItems()).singleElement().satisfies(item ->
            assertThat(item.cost()).isZero());
        assertThat(view.rentalItems()).singleElement().satisfies(item ->
            assertThat(item.rentalPrice()).isEqualTo(view.totalRentalPrice()));

        RecommendationJob request = requests.findById(response.requestId()).orElseThrow();
        RecommendationResult saved = recommendations
            .findFirstByRequestIdOrderByRank(request.getId()).orElseThrow();
        List<RecommendationItem> savedItems = items
            .findAllByRecommendationIdOrderBySortOrder(saved.getId());

        assertThat(request.getStatus()).isEqualTo(RecommendationJob.Status.COMPLETED);
        assertThat(requestWardrobes.findById(
            new RecommendationRequestWardrobeId(request.getId(), wardrobe.getId()))).isPresent();
        assertThat(saved.getMatchingScore()).isEqualByComparingTo(new BigDecimal("0.8500"));
        assertThat(savedItems).hasSize(2);
        assertThat(savedItems.get(0).getSourceType()).isEqualTo("WARDROBE");
        assertThat(savedItems.get(0).getWardrobeItem()).isNotNull();
        assertThat(savedItems.get(0).getProductVariant()).isNull();
        assertThat(savedItems.get(0).getPriceSnapshot()).isNull();
        assertThat(savedItems.get(1).getSourceType()).isEqualTo("RENTAL_PRODUCT");
        assertThat(savedItems.get(1).getWardrobeItem()).isNull();
        assertThat(savedItems.get(1).getProductVariant()).isNotNull();
        assertThat(savedItems.get(1).getPriceSnapshot()).isEqualTo(view.totalRentalPrice());
    }

    @Test
    void returnsNoMatchWithoutCallingAiWhenCandidateSetIsEmpty() {
        long userId = 8202L;
        WardrobeItem wardrobe = wardrobeItems.save(wardrobe(userId, "White Shirt"));

        CustomerRecommendationResponse response = service.recommend(userId,
            new CustomerRecommendationRequest(
                "INTERVIEW", null, "Formal", "XXXL", 30_000L, List.of(wardrobe.getId())));

        assertThat(response.status()).isEqualTo(
            com.example.minip.recommendation.ai.AiRecommendationResult.Status.NO_MATCH);
        assertThat(response.recommendations()).isEmpty();
        assertThat(requests.findById(response.requestId()).orElseThrow().getStatus())
            .isEqualTo(RecommendationJob.Status.COMPLETED);
        assertThat(recommendations.findFirstByRequestIdOrderByRank(response.requestId())).isEmpty();
    }

    private WardrobeItem wardrobe(long userId, String name) {
        return new WardrobeItem(userId, name, "BOTTOM", "BLACK", "ALL", "",
            "wardrobe.jpg", "image/jpeg", new byte[] {1, 2, 3});
    }
}
