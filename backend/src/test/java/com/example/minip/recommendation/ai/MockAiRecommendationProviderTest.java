package com.example.minip.recommendation.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.minip.ai.VisionFeatures;
import java.util.List;
import org.junit.jupiter.api.Test;

class MockAiRecommendationProviderTest {
    private final MockAiRecommendationProvider provider = new MockAiRecommendationProvider();

    @Test
    void ranksInterviewProductAndReturnsUpToThreeOutfits() {
        AiRecommendationContext context = new AiRecommendationContext(
            "INTERVIEW", List.of("Formal"), "M", 50_000L,
            List.of(new AiRecommendationContext.WardrobeCandidate(
                11L, "검정 슬랙스", new VisionFeatures(
                    "BOTTOM", "UNKNOWN", "BLACK", "UNKNOWN",
                    "UNKNOWN", "UNKNOWN", "UNKNOWN", 1.0))),
            List.of(
                candidate(1L, "그룹 캐주얼 세트", "ACCESSORY", 12_000L),
                candidate(2L, "베이직 면접 정장 세트", "OUTER", 29_000L),
                candidate(3L, "비즈니스 캐주얼 세트", "TOP", 24_000L),
                candidate(4L, "세미 포멀 행사 세트", "SHOES", 26_000L)
            ));

        AiRecommendationResult result = provider.recommend(context);

        assertThat(result.status()).isEqualTo(AiRecommendationResult.Status.COMPLETED);
        assertThat(result.recommendations()).hasSize(3);
        assertThat(result.recommendations().getFirst().outfitTitle()).contains("면접", "정장");
        assertThat(result.recommendations().getFirst().matchScore()).isEqualTo(94);
        assertThat(result.recommendations().getFirst().wardrobeItemIds()).containsExactly(11L);
    }

    private AiRecommendationContext.RentalCandidate candidate(Long id, String name,
                                                               String category, Long price) {
        return new AiRecommendationContext.RentalCandidate(
            id, id, "FITLY", name, category, "M", price, 10);
    }
}
