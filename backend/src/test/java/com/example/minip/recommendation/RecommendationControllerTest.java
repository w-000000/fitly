package com.example.minip.recommendation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RecommendationControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void recommendsPersonalProductsWithinBudget() throws Exception {
        String start = LocalDate.now().plusDays(1).toString();
        String end = LocalDate.now().plusDays(3).toString();
        mvc.perform(post("/api/recommendations/rules").contentType(MediaType.APPLICATION_JSON).content("""
            {"purpose":"PERSONAL","personalSituation":"INTERVIEW","size":"M","budget":30000,
             "rentalStartDate":"%s","rentalEndDate":"%s"}
            """.formatted(start, end)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rentalDays").value(3))
            .andExpect(jsonPath("$.model").value("fitly-rules-v1"))
            .andExpect(jsonPath("$.products.length()").value(2))
            .andExpect(jsonPath("$.products[0].rentalPrice").value(24000));
    }

    @Test
    void rejectsEventForFewerThanTwoPeople() throws Exception {
        String date = LocalDate.now().plusDays(1).toString();
        mvc.perform(post("/api/recommendations/rules").contentType(MediaType.APPLICATION_JSON).content("""
            {"purpose":"EVENT","groupSizes":{"M":1},"budget":50000,
             "rentalStartDate":"%s","rentalEndDate":"%s"}
            """.formatted(date, date)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("단체 대여는 사이즈별 수량을 합해 2명 이상이어야 합니다."));
    }

    @Test
    void rejectsEndDateBeforeStartDate() throws Exception {
        String start = LocalDate.now().plusDays(2).toString();
        String end = LocalDate.now().plusDays(1).toString();
        mvc.perform(post("/api/recommendations/rules").contentType(MediaType.APPLICATION_JSON).content("""
            {"purpose":"PERSONAL","personalSituation":"WORK","size":"L","budget":50000,
             "rentalStartDate":"%s","rentalEndDate":"%s"}
            """.formatted(start, end)))
            .andExpect(status().isBadRequest());
    }
}
