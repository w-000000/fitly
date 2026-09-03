package com.example.minip.catalog;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.minip.ai.VisionFeatures;
import com.example.minip.business.ReferenceDataService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ProductDescriptionIntegrationTest {
    @Autowired ProductDescriptionService service;
    @Autowired ProductDescriptionGenerationRepository generations;
    @Autowired ReferenceDataService references;

    @Test
    void persistsEditableCompletedDraftWithoutUpdatingProduct() {
        long businessId = 8301L;
        ReferenceDataService.BusinessContext context = references.ensureBusiness(
            businessId, new BigDecimal("0.7000"));
        ProductDescriptionRequest request = new ProductDescriptionRequest(
            businessId,
            context.member().getId(),
            null,
            "https://image.test/navy-blazer.jpg",
            new ProductDescriptionRequest.ProductInput(
                "Navy Single Blazer", "FITLY", "OUTER", "싱글 브레스트"),
            new VisionFeatures("OUTER", "BLAZER", "NAVY", "REGULAR",
                "STRAIGHT", "SOLID", "FORMAL", 0.93)
        );

        ProductDescriptionResponse response = service.generate(businessId, request);
        ProductDescriptionGeneration generation = generations.findById(response.generationId())
            .orElseThrow();

        assertThat(response.status()).isEqualTo(ProductDescriptionGeneration.Status.COMPLETED);
        assertThat(response.generatedDescription())
            .contains("[헤드카피]", "[실루엣 및 원단]", "[추천 TPO 및 스타일링]", "[디테일]");
        assertThat(generation.getStatus()).isEqualTo(ProductDescriptionGeneration.Status.COMPLETED);
        assertThat(generation.getGeneratedDescription()).isEqualTo(response.generatedDescription());
    }
}
