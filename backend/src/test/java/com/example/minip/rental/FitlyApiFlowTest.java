package com.example.minip.rental;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class FitlyApiFlowTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    @Test
    void productRentalReturnAndLaundryRestoreStock() throws Exception {
        JsonNode product = body(mvc.perform(post("/api/products")
                .header("X-Actor-Role", "ROLE_PARTNER").contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"partnerId":101,"name":"면접용 블레이저","category":"JACKET","retailPrice":120000,"rentalPrice":30000,"settlementRate":0.7,"imageUrl":"https://example.com/jacket.jpg"}
                    """))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long productId = product.path("product").path("id").asLong();

        JsonNode variant = body(mvc.perform(post("/api/products/{id}/variants", productId)
                .header("X-Actor-Role", "ROLE_PARTNER").contentType(MediaType.APPLICATION_JSON)
                .content("{\"size\":\"M\",\"stock\":3}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString());
        long variantId = variant.path("id").asLong();

        JsonNode rental = body(mvc.perform(post("/api/rentals")
                .header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":" + variantId + ",\"quantity\":2,\"startDate\":\"2026-09-10\",\"endDate\":\"2026-09-17\",\"shippingAddress\":\"서울시\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.endDate").value("2026-09-17"))
            .andExpect(jsonPath("$.totalAmount").value(60000))
            .andReturn().getResponse().getContentAsString());
        long rentalId = rental.path("id").asLong();

        mvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk()).andExpect(jsonPath("$.variants[0].availableStock").value(1));

        mvc.perform(get("/api/products/variants/{id}/availability", variantId)
                .param("startDate", "2026-09-12").param("endDate", "2026-09-15").param("quantity", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.availableQuantity").value(1))
            .andExpect(jsonPath("$.available").value(false));

        mvc.perform(post("/api/rentals/{id}/return-request", rentalId)
                .header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "7"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("RETURN_REQUESTED"));

        mvc.perform(post("/api/laundry/inspections")
                .header("X-Actor-Role", "ROLE_ADMIN").contentType(MediaType.APPLICATION_JSON)
                .content("{\"rentalOrderId\":" + rentalId + ",\"damageGrade\":\"NONE\",\"notes\":\"정상\",\"cleaned\":true}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.cleaned").value(true));

        mvc.perform(get("/api/products/{id}", productId))
            .andExpect(status().isOk()).andExpect(jsonPath("$.variants[0].availableStock").value(3));

        mvc.perform(get("/api/rentals/partner/101/revenue").header("X-Actor-Role", "ROLE_PARTNER"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.rentalRevenue").value(60000.0));

        mvc.perform(get("/api/rentals/partner/101/settlements").header("X-Actor-Role", "ROLE_PARTNER"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.settlementAmount").value(42000.0));
    }

    @Test
    void rejectsRoleWithoutPermission() throws Exception {
        mvc.perform(post("/api/products").header("X-Actor-Role", "ROLE_CUSTOMER")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"partnerId\":1,\"name\":\"상품\",\"category\":\"TOP\",\"retailPrice\":10000,\"rentalPrice\":2500,\"settlementRate\":0.5}"))
            .andExpect(status().isForbidden());
    }

    @Test
    void rejectsRentalEndDateBeforeStartDate() throws Exception {
        mvc.perform(post("/api/rentals")
                .header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "7")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"variantId\":1,\"quantity\":1,\"startDate\":\"2026-09-10\",\"endDate\":\"2026-09-09\",\"shippingAddress\":\"서울시\",\"insurance\":false}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("대여 종료일은 시작일과 같거나 이후여야 합니다."));
    }

    private JsonNode body(String value) throws Exception { return json.readTree(value); }
}
