package com.example.minip.recommendation;

import com.example.minip.catalog.Product;
import com.example.minip.catalog.ProductRepository;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class RecommendationService {
    private final ProductRepository products;

    public RecommendationService(ProductRepository products) {
        this.products = products;
    }

    @Transactional(readOnly = true)
    public RecommendationResponse recommend(RecommendationRequest request) {
        validate(request);
        int rentalDays = (int) ChronoUnit.DAYS.between(request.rentalStartDate(), request.rentalEndDate()) + 1;
        var matches = products.findByPurposeAndRentalPriceLessThanEqualAndStockGreaterThanOrderByRentalPriceAsc(
            request.purpose(), request.budget(), 0
        );
        var results = matches.stream().map(this::toResult).toList();
        String message = request.purpose() == RentalPurpose.PERSONAL
            ? "선택한 개인 일정과 상황에 적합한 의류를 추천했어요."
            : "참여 인원과 행사 성격에 어울리는 단체 의류를 추천했어요.";
        return new RecommendationResponse(message, "fitly-rules-v1", rentalDays, results);
    }

    private void validate(RecommendationRequest request) {
        if (request.rentalEndDate().isBefore(request.rentalStartDate())) {
            badRequest("대여 종료일은 시작일과 같거나 이후여야 합니다.");
        }
        if (request.purpose() == RentalPurpose.PERSONAL
            && (request.personalSituation() == null || request.size() == null)) {
            badRequest("개인 대여에는 상황과 사이즈가 필요합니다.");
        }
        if (request.purpose() == RentalPurpose.EVENT) {
            int groupCount = request.groupSizes() == null ? 0
                : request.groupSizes().values().stream().mapToInt(value -> value == null ? 0 : value).sum();
            if (groupCount < 2) badRequest("단체 대여는 사이즈별 수량을 합해 2명 이상이어야 합니다.");
        }
    }

    private RecommendationResponse.ProductResult toResult(Product product) {
        return new RecommendationResponse.ProductResult(product.getId(), product.getCategory(), product.getName(),
            product.getDescription(), product.getRentalPrice(), product.getPurchasePrice(),
            product.getRecommendationReason(), product.getStock());
    }

    private void badRequest(String message) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
