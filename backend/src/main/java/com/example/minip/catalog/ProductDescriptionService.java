package com.example.minip.catalog;

import com.example.minip.ai.AiIntegrationException;
import com.example.minip.business.BusinessMember;
import com.example.minip.business.BusinessMemberRepository;
import com.example.minip.catalog.ai.ProductDescriptionContext;
import com.example.minip.catalog.ai.ProductDescriptionProvider;
import com.example.minip.catalog.ai.ProductDescriptionResult;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductDescriptionService {
    private final ProductDescriptionGenerationRepository generations;
    private final BusinessMemberRepository businessMembers;
    private final ProductRepository products;
    private final ProductDescriptionProvider provider;
    private final TransactionTemplate transactions;

    public ProductDescriptionService(ProductDescriptionGenerationRepository generations,
                                     BusinessMemberRepository businessMembers,
                                     ProductRepository products, ProductDescriptionProvider provider,
                                     PlatformTransactionManager transactionManager) {
        this.generations = generations;
        this.businessMembers = businessMembers;
        this.products = products;
        this.provider = provider;
        this.transactions = new TransactionTemplate(transactionManager);
    }

    public ProductDescriptionResponse generate(Long userId, ProductDescriptionRequest request) {
        validateVision(request);
        Long generationId = transactions.execute(status -> start(userId, request));
        if (generationId == null) {
            throw new IllegalStateException("상품 설명 생성을 준비하지 못했습니다.");
        }
        try {
            ProductDescriptionResult result = provider.generate(toContext(request));
            String description = assemble(result);
            return transactions.execute(status -> complete(generationId, description));
        } catch (RuntimeException exception) {
            markFailed(generationId);
            throw exception;
        }
    }

    private Long start(Long userId, ProductDescriptionRequest request) {
        BusinessMember member = businessMembers.findByIdAndBusinessIdAndUserIdAndStatus(
            request.businessMemberId(), request.businessId(), userId, "ACTIVE"
        ).orElseThrow(() -> new ResponseStatusException(
            HttpStatus.FORBIDDEN, "해당 사업자의 활성 Business Member가 아닙니다."));
        Product product = request.productId() == null ? null
            : products.findByIdAndBusinessId(request.productId(), request.businessId())
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "해당 사업자의 상품을 찾을 수 없습니다."));
        ProductDescriptionGeneration generation = generations.save(
            new ProductDescriptionGeneration(member.getBusiness(), member, product,
                request.inputImageUrl().trim())
        );
        generation.startProcessing();
        return generation.getId();
    }

    private ProductDescriptionContext toContext(ProductDescriptionRequest request) {
        ProductDescriptionRequest.ProductInput product = request.product();
        return new ProductDescriptionContext(product.productName().trim(), product.brandName().trim(),
            Product.normalizeCategory(product.category()), product.providedInformation(),
            request.visionFeatures());
    }

    private String assemble(ProductDescriptionResult result) {
        if (result == null || result.status() != ProductDescriptionResult.Status.COMPLETED
            || blank(result.headline()) || blank(result.stylingAndTpo())) {
            throw new AiIntegrationException(AiIntegrationException.Reason.INVALID_RESPONSE);
        }
        List<String> sections = new ArrayList<>();
        sections.add("[헤드카피]\n" + result.headline().trim());
        if (!blank(result.silhouetteAndFabric())) {
            sections.add("[실루엣 및 원단]\n" + result.silhouetteAndFabric().trim());
        }
        sections.add("[추천 TPO 및 스타일링]\n" + result.stylingAndTpo().trim());
        List<String> details = result.details() == null ? List.of()
            : result.details().stream().filter(value -> !blank(value)).map(String::trim).toList();
        if (!details.isEmpty()) {
            sections.add("[디테일]\n" + details.stream()
                .map(value -> "- " + value).reduce((left, right) -> left + "\n" + right).orElse(""));
        }
        return String.join("\n\n", sections);
    }

    private ProductDescriptionResponse complete(Long generationId, String description) {
        ProductDescriptionGeneration generation = generations.findById(generationId).orElseThrow();
        generation.complete(description);
        return new ProductDescriptionResponse(generation.getId(), generation.getStatus(), description);
    }

    private void markFailed(Long generationId) {
        transactions.executeWithoutResult(status ->
            generations.findById(generationId).ifPresent(ProductDescriptionGeneration::fail));
    }

    private void validateVision(ProductDescriptionRequest request) {
        if (!request.visionFeatures().hasUsableFeature()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST, "상품 설명 생성에 사용할 Vision 정보가 부족합니다.");
        }
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
