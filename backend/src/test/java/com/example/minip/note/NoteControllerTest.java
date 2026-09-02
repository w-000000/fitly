package com.example.minip.note;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class NoteControllerTest {
    @Autowired MockMvc mvc;

    @Test
    void createsAndListsNote() throws Exception {
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"테스트 아이디어\",\"content\":\"사용자 문제를 해결합니다.\"}"))
            .andExpect(status().isCreated()).andExpect(jsonPath("$.id").isNumber());

        mvc.perform(get("/api/notes")).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].title").value("테스트 아이디어"));
    }

    @Test
    void rejectsBlankNote() throws Exception {
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\",\"content\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
}

