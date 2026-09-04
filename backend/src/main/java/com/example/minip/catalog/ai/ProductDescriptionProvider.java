package com.example.minip.catalog.ai;

public interface ProductDescriptionProvider {
    ProductDescriptionResult generate(ProductDescriptionContext context);
}
