package com.example.minip.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.minip.business.ReferenceDataService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CandidateFilteringTest {
    @Autowired ProductRepository products;
    @Autowired ProductVariantRepository variants;
    @Autowired ReferenceDataService references;

    @Test
    void excludesOutOfStockAndSizeMismatchCandidates() {
        ReferenceDataService.BusinessContext context = references.ensureBusiness(
            8101L, new BigDecimal("0.7000"));
        Product outOfStockProduct = products.save(product(context, "재고 없음 M", 1000));
        Product wrongSizeProduct = products.save(product(context, "L 사이즈", 1000));
        ProductVariant outOfStock = variants.save(new ProductVariant(outOfStockProduct, "M", 0));
        ProductVariant wrongSize = variants.save(new ProductVariant(wrongSizeProduct, "L", 2));

        List<ProductVariant> candidates = variants.findRecommendationCandidates(
            "M", null, PageRequest.of(0, 20));

        assertThat(candidates).allSatisfy(candidate -> {
            assertThat(candidate.getStatus()).isEqualTo("ACTIVE");
            assertThat(candidate.getAvailableStock()).isPositive();
            assertThat(candidate.getSizeName()).isEqualToIgnoringCase("M");
        });
        assertThat(candidates).extracting(ProductVariant::getId)
            .doesNotContain(outOfStock.getId(), wrongSize.getId());
    }

    @Test
    void excludesProductsOverBudget() {
        List<ProductVariant> candidates = variants.findRecommendationCandidates(
            "M", 25_000L, PageRequest.of(0, 20));

        assertThat(candidates).isNotEmpty();
        assertThat(candidates).allSatisfy(candidate ->
            assertThat(candidate.getProduct().getRentalPrice())
                .isLessThanOrEqualTo(BigDecimal.valueOf(25_000L)));
        assertThat(candidates).extracting(candidate -> candidate.getProduct().getRentalPrice())
            .doesNotContain(BigDecimal.valueOf(26_000L), BigDecimal.valueOf(29_000L));
    }

    private Product product(ReferenceDataService.BusinessContext context, String name,
                            long rentalPrice) {
        return new Product(context.business(), context.member(), name, "FITLY", "OUTER", name,
            BigDecimal.valueOf(10_000), BigDecimal.valueOf(rentalPrice), "https://image.test/item.jpg");
    }
}
