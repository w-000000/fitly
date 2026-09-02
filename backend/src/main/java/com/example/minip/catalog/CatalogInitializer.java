package com.example.minip.catalog;

import com.example.minip.recommendation.RentalPurpose;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CatalogInitializer implements ApplicationRunner {
    private final ProductRepository products;

    public CatalogInitializer(ProductRepository products) {
        this.products = products;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (products.count() > 0) return;

        products.saveAll(List.of(
            new Product(RentalPurpose.PERSONAL, "면접", "베이직 면접 정장 세트",
                "재킷, 슬랙스, 셔츠가 포함된 면접용 구성입니다.", 29000, 149000,
                "단정한 디자인으로 첫 면접에 적합합니다.", 10),
            new Product(RentalPurpose.PERSONAL, "비즈니스", "비즈니스 캐주얼 세트",
                "재킷과 슬랙스로 구성된 실용적인 세트입니다.", 24000, 119000,
                "인턴 면접과 출근 복장으로 활용하기 좋습니다.", 8),
            new Product(RentalPurpose.EVENT, "행사·모임", "그룹 캐주얼 세트",
                "워크숍, 체육대회 및 야외 모임에 적합합니다.", 12000, 59000,
                "움직이기 편하고 여러 명이 함께 맞춰 입기 좋습니다.", 100),
            new Product(RentalPurpose.EVENT, "행사·모임", "세미 포멀 행사 세트",
                "결혼식과 공식 모임에 어울리는 구성입니다.", 26000, 129000,
                "부담스럽지 않으면서 깔끔한 인상을 줍니다.", 40)
        ));
    }
}
