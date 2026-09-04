package com.example.minip.recommendation.ai;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
        List<Long> wardrobeIds = context.wardrobeItems().stream()
            .map(AiRecommendationContext.WardrobeCandidate::wardrobeItemId)
            .toList();
        List<AiRecommendationContext.RentalCandidate> ranked = context.availableCandidates().stream()
            .sorted(Comparator.comparingInt(
                (AiRecommendationContext.RentalCandidate candidate) -> score(context, candidate)).reversed()
                .thenComparing(AiRecommendationContext.RentalCandidate::rentalPrice)
                .thenComparing(AiRecommendationContext.RentalCandidate::productVariantId))
            .limit(3)
            .toList();
        List<AiRecommendationResult.Outfit> outfits = ranked.stream()
            .map(candidate -> outfit(context, candidate, wardrobeIds, ranked.indexOf(candidate) + 1))
            .toList();
        return new AiRecommendationResult(AiRecommendationResult.Status.COMPLETED,
            outfits, List.of());
    }

    private AiRecommendationResult.Outfit outfit(AiRecommendationContext context,
                                                   AiRecommendationContext.RentalCandidate candidate,
                                                   List<Long> wardrobeIds, int rank) {
        String style = context.preferredStyles().isEmpty() ? "선택한" : context.preferredStyles().getFirst();
        String reason = "%s 분위기와 %s 상황을 반영하고, 내 옷 %d개를 활용해 필요한 상품만 골랐습니다."
            .formatted(style, tpoLabel(context.tpo()), wardrobeIds.size());
        return new AiRecommendationResult.Outfit(rank,
            tpoLabel(context.tpo()) + "을 위한 " + candidate.productName() + " 코디",
            Math.max(75, 94 - ((rank - 1) * 6)), reason, wardrobeIds,
            List.of(candidate.productVariantId()));
    }

    private int score(AiRecommendationContext context,
                      AiRecommendationContext.RentalCandidate candidate) {
        String text = (candidate.productName() + " " + candidate.category()).toLowerCase(Locale.ROOT);
        String tpo = context.tpo().toUpperCase(Locale.ROOT);
        int result = candidate.availableStock() > 0 ? 10 : 0;
        result += switch (tpo) {
            case "INTERVIEW" -> containsAny(text, "면접", "정장", "business", "outer") ? 40 : 0;
            case "WORK" -> containsAny(text, "비즈니스", "출근", "business", "top") ? 40 : 0;
            case "DATE", "GUEST" -> containsAny(text, "행사", "포멀", "formal", "shoes") ? 40 : 0;
            case "DAILY" -> containsAny(text, "캐주얼", "casual", "accessory") ? 40 : 0;
            default -> 0;
        };
        return result;
    }

    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String tpoLabel(String value) {
        return switch (value.toUpperCase(Locale.ROOT)) {
            case "INTERVIEW" -> "면접";
            case "WORK" -> "출근";
            case "DATE" -> "데이트";
            case "GUEST" -> "하객";
            case "DAILY" -> "일상";
            default -> value;
        };
    }
}
