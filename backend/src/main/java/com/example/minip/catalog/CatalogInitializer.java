package com.example.minip.catalog;

import com.example.minip.business.ReferenceDataService;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class CatalogInitializer implements ApplicationRunner {
    private static final Long DEMO_BUSINESS_ID = 1000L;

    private final ProductRepository products;
    private final ProductVariantRepository variants;
    private final ReferenceDataService references;

    public CatalogInitializer(ProductRepository products, ProductVariantRepository variants,
                              ReferenceDataService references) {
        this.products = products;
        this.variants = variants;
        this.references = references;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (products.count() > 0) {
            return;
        }

        ReferenceDataService.BusinessContext context = references.ensureBusiness(
            DEMO_BUSINESS_ID, new BigDecimal("0.7000")
        );
        List<Product> saved = products.saveAll(List.of(
            product(context, "베이직 면접 정장 세트", "OUTER", 149000, 29000,
                "단정한 디자인으로 첫 면접에 적합합니다."),
            product(context, "비즈니스 캐주얼 세트", "TOP", 119000, 24000,
                "인턴 면접과 출근 복장으로 활용하기 좋습니다."),
            product(context, "그룹 캐주얼 세트", "ACCESSORY", 59000, 12000,
                "움직이기 편하고 여러 명이 함께 맞춰 입기 좋습니다."),
            product(context, "세미 포멀 행사 세트", "SHOES", 129000, 26000,
                "부담스럽지 않으면서 깔끔한 인상을 줍니다.")
        ));
        int[] stock = {10, 8, 100, 40};
        for (int index = 0; index < saved.size(); index++) {
            variants.save(new ProductVariant(saved.get(index), "M", stock[index]));
        }
    }

    private Product product(ReferenceDataService.BusinessContext context, String name,
                            String category, int retailPrice, int rentalPrice, String description) {
        return new Product(context.business(), context.member(), name, "FITLY", category,
            description, BigDecimal.valueOf(retailPrice), BigDecimal.valueOf(rentalPrice), "");
    }
}
