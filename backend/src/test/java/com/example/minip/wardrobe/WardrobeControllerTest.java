package com.example.minip.wardrobe;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class WardrobeControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void customerUploadsAndListsOwnClothes() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", MediaType.APPLICATION_JSON_VALUE,
            "{\"name\":\"검정 슬랙스\",\"category\":\"PANTS\",\"color\":\"BLACK\",\"season\":\"SPRING,FALL\"}".getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "slacks.png", MediaType.IMAGE_PNG_VALUE,
            new byte[] {(byte) 0x89, 0x50, 0x4e, 0x47});

        mvc.perform(multipart("/api/wardrobe/items").file(metadata).file(image)
                .header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "11"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("검정 슬랙스"))
            .andExpect(jsonPath("$.imageUrl").value("/api/wardrobe/items/1/image"));

        mvc.perform(get("/api/wardrobe/items").header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "11"))
            .andExpect(status().isOk()).andExpect(jsonPath("$[0].category").value("PANTS"));

        mvc.perform(get("/api/wardrobe/items/1/image").header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "11"))
            .andExpect(status().isOk()).andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    void rejectsNonImageUpload() throws Exception {
        MockMultipartFile metadata = new MockMultipartFile("metadata", "", MediaType.APPLICATION_JSON_VALUE,
            "{\"name\":\"파일\",\"category\":\"ETC\"}".getBytes());
        MockMultipartFile file = new MockMultipartFile("image", "note.txt", MediaType.TEXT_PLAIN_VALUE, "text".getBytes());
        mvc.perform(multipart("/api/wardrobe/items").file(metadata).file(file)
                .header("X-Actor-Role", "ROLE_CUSTOMER").header("X-User-Id", "11"))
            .andExpect(status().isUnsupportedMediaType());
    }
}
