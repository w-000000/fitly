package com.example.minip.recommendation.ai;

import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "fitly.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockAiRecommendationProvider implements AiRecommendationProvider {
    @Override
    public AiRecommendationResult recommend(AiRecommendationContext context) {
        if (context.availableCandidates().isEmpty()) {
            return new AiRecommendationResult(AiRecommendationResult.Status.NO_MATCH, List.of(), List.of());
        }
        AiRecommendationContext.RentalCandidate candidate = context.availableCandidates().getFirst();
        List<Long> wardrobeIds = context.wardrobeItems().stream()
            .map(AiRecommendationContext.WardrobeCandidate::wardrobeItemId)
            .toList();
        AiRecommendationResult.Outfit outfit = new AiRecommendationResult.Outfit(
            1,
            context.tpo() + "에 어울리는 " + candidate.productName() + " 코디",
            85,
            "선택한 내 옷을 활용하고 실제 대여 가능한 상품으로 필요한 구성을 최소화했습니다.",
            wardrobeIds,
            List.of(candidate.productVariantId())
        );
        return new AiRecommendationResult(AiRecommendationResult.Status.COMPLETED,
            List.of(outfit), List.of());
    }
}
