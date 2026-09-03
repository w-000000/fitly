package com.example.minip;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.fasterxml.jackson.databind.*;
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
 @Test void customerClosetSavedLookAndGroupRequest() throws Exception {
  MockMultipartFile image=new MockMultipartFile("image","slacks.jpg","image/jpeg",new byte[]{1,2,3});
  MockMultipartFile metadata=new MockMultipartFile("metadata","","application/json",
      "{\"name\":\"Black Slacks\",\"category\":\"PANTS\",\"color\":\"BLACK\"}".getBytes());
  mvc.perform(multipart("/api/wardrobe/items").file(metadata).file(image)
      .header("X-Actor-Role","ROLE_CUSTOMER").header("X-User-Id","71"))
    .andExpect(status().isCreated()).andExpect(jsonPath("$.name").value("Black Slacks"));
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
  mvc.perform(post("/api/products/ai-description").header("X-Actor-Role","ROLE_PARTNER")
      .contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Navy Blazer\",\"brand\":\"FITLY\",\"category\":\"OUTER\"}"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.mock").value(true)).andExpect(jsonPath("$.description").isNotEmpty());
  mvc.perform(get("/api/admin/dashboard").header("X-Actor-Role","ROLE_ADMIN"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.productCount").isNumber());
  mvc.perform(get("/api/partner/dashboard").param("partnerId","101").header("X-Actor-Role","ROLE_PARTNER"))
    .andExpect(status().isOk()).andExpect(jsonPath("$.partnerId").value(101));
 }
}
