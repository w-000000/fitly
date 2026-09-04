package com.example.minip.catalog.ai;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "fitly.ai", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockProductDescriptionProvider implements ProductDescriptionProvider {
    @Override
    public ProductDescriptionResult generate(ProductDescriptionContext context) {
        List<String> details = new ArrayList<>();
        if (known(context.visionFeatures().primaryColor())) {
            details.add(context.visionFeatures().primaryColor() + " 색상");
        }
        if (known(context.visionFeatures().pattern())) {
            details.add(context.visionFeatures().pattern() + " 패턴");
        }
        return new ProductDescriptionResult(
            ProductDescriptionResult.Status.COMPLETED,
            "깔끔한 인상의 " + context.productName(),
            silhouette(context),
            "다양한 일상과 격식을 갖춘 상황에서 보유 의류와 조합해 활용할 수 있습니다.",
            details,
            List.of("MD_REVIEW_REQUIRED")
        );
    }

    private String silhouette(ProductDescriptionContext context) {
        String fit = context.visionFeatures().fit();
        String silhouette = context.visionFeatures().silhouette();
        if (known(fit) || known(silhouette)) {
            return "확인된 " + value(fit) + " 핏과 " + value(silhouette) + " 실루엣이 특징입니다.";
        }
        return "이미지와 입력 정보에서 확인 가능한 디자인을 중심으로 구성된 상품입니다.";
    }

    private String value(String input) {
        return known(input) ? input : "기본";
    }

    private boolean known(String input) {
        return input != null && !input.isBlank() && !"UNKNOWN".equalsIgnoreCase(input);
    }
}
