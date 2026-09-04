package com.example.minip;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.example.minip.business.ReferenceDataService;
import com.fasterxml.jackson.databind.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("test")
class WireframeApiTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json;
 @Autowired ReferenceDataService references;
 @Test void customerClosetSavedLookAndGroupRequest() throws Exception {
  MockMultipartFile image=new MockMultipartFile("image","slacks.jpg","image/jpeg",new byte[]{1,2,3});
  MockMultipartFile metadata=new MockMultipartFile("metadata","","application/json",
      "{\"name\":\"Black Slacks\",\"category\":\"PANTS\",\"color\":\"BLACK\"}".getBytes());
  String wardrobeResponse=mvc.perform(multipart("/api/wardrobe/items").file(metadata).file(image)
      .header("X-Actor-Role","ROLE_CUSTOMER").header("X-User-Id","71"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Black Slacks"))
    .andReturn().getResponse().getContentAsString();
  long wardrobeId=json.readTree(wardrobeResponse).path("id").asLong();
  mvc.perform(post("/api/recommendations")
      .header("X-Actor-Role","ROLE_CUSTOMER").header("X-User-Id","71")
      .contentType(MediaType.APPLICATION_JSON).content("""
          {"tpo":"INTERVIEW","style":"Formal","size":"M","budget":30000,
           "wardrobeItemIds":[%d]}
          """.formatted(wardrobeId)))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.status").value("COMPLETED"))
    .andExpect(jsonPath("$.recommendations[0].matchScore").value(85))
    .andExpect(jsonPath("$.recommendations[0].wardrobeItems[0].cost").value(0))
    .andExpect(jsonPath("$.recommendations[0].rentalItems[0].productVariantId").isNumber())
    .andExpect(jsonPath("$.recommendations[0].totalRentalPrice").value(12000));
  String job=mvc.perform(post("/api/recommendations/jobs").header("X-Actor-Role","ROLE_CUSTOMER").header("X-User-Id","71")
      .contentType(MediaType.APPLICATION_JSON).content("{\"tpo\":\"면접\",\"preferredStyle\":\"Formal\",\"wardrobeDescription\":\"Black Slacks\"}"))
    .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  long jobId=json.readTree(job).path("request").path("id").asLong();
  mvc.perform(put("/api/recommendations/jobs/{jobId}/looks/LOOK-01/saved",jobId).header("X-Actor-Role","ROLE_CUSTOMER").header("X-User-Id","71")
      .contentType(MediaType.APPLICATION_JSON).content("{\"title\":\"Navy Interview\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.saved").value(true));
  mvc.perform(post("/api/group-rentals").header("X-Actor-Role","ROLE_CUSTOMER").header("X-User-Id","71")
      .contentType(MediaType.APPLICATION_JSON).content("{\"purpose\":\"연구실\",\"startDate\":\"2026-09-12\",\"endDate\":\"2026-11-07\",\"headcount\":8,\"requestedItems\":\"연구복, 안전화\",\"contactName\":\"김민수\",\"contactPhone\":\"010-0000-0000\"}"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.status").value("RECEIVED"));
 }
 @Test void partnerAiDescriptionAndDashboards() throws Exception {
  long businessId=8501L;
  ReferenceDataService.BusinessContext context=references.ensureBusiness(
      businessId,new BigDecimal("0.7000"));
  mvc.perform(post("/api/products/ai-description")
      .header("X-Actor-Role","ROLE_PARTNER").header("X-User-Id",businessId)
      .contentType(MediaType.APPLICATION_JSON).content("""
          {"businessId":%d,"businessMemberId":%d,"productId":null,
           "inputImageUrl":"https://image.test/navy-blazer.jpg",
           "product":{"productName":"Navy Blazer","brandName":"FITLY","category":"OUTER",
                      "providedInformation":"싱글 브레스트"},
           "visionFeatures":{"category":"OUTER","subcategory":"BLAZER",
              "primaryColor":"NAVY","fit":"REGULAR","silhouette":"STRAIGHT",
              "pattern":"SOLID","styleType":"FORMAL","confidence":0.93}}
          """.formatted(businessId,context.member().getId())))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.status").value("COMPLETED"))
    .andExpect(jsonPath("$.generatedDescription").isNotEmpty());
  mvc.perform(get("/api/admin/dashboard").header("X-Actor-Role","ROLE_ADMIN"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.productCount").isNumber());
  mvc.perform(get("/api/partner/dashboard").param("partnerId","101").header("X-Actor-Role","ROLE_PARTNER"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.partnerId").value(101));
 }
}
