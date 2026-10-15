package com.management.notes;

import com.management.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class NoteApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void createsNoteWithNormalizedTags() throws Exception {
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "  Groceries ", "content": "milk, eggs", "tags": ["Home", "shopping"]}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Groceries"))
                .andExpect(jsonPath("$.pinned").value(false))
                .andExpect(jsonPath("$.tags", contains("home", "shopping")));
    }

    @Test
    void searchesByTextAndTag() throws Exception {
        create("Spring tips", "Use records for DTOs", "java");
        create("Shopping", "Buy spring onions", "home");
        create("Trip", "Pack a jacket", "travel");

        mvc.perform(get("/api/notes").param("q", "SPRING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));

        mvc.perform(get("/api/notes").param("q", "spring").param("tag", "java"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Spring tips"));
    }

    @Test
    void treatsLikeWildcardsLiterally() throws Exception {
        create("Discount", "Save 50% today", "deals");
        create("Other", "Nothing special", "misc");

        mvc.perform(get("/api/notes").param("q", "%"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void pinnedNotesComeFirst() throws Exception {
        long first = create("First", "a", "x");
        create("Second", "b", "x");

        mvc.perform(put("/api/notes/{id}/pin", first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pinned").value(true));

        mvc.perform(get("/api/notes"))
                .andExpect(jsonPath("$.content[0].title").value("First"));

        mvc.perform(delete("/api/notes/{id}/pin", first))
                .andExpect(jsonPath("$.pinned").value(false));
    }

    @Test
    void countsNotesPerTag() throws Exception {
        create("A", "a", "java");
        create("B", "b", "java");
        create("C", "c", "travel");

        mvc.perform(get("/api/notes/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tag").value("java"))
                .andExpect(jsonPath("$[0].count").value(2))
                .andExpect(jsonPath("$[1].tag").value("travel"));
    }

    @Test
    void rejectsInvalidTags() throws Exception {
        mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "t", "content": "c", "tags": ["<script>"]}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void updatesAndDeletesNote() throws Exception {
        long id = create("Draft", "v1", "work");

        mvc.perform(put("/api/notes/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Final", "content": "v2", "tags": []}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Final"))
                .andExpect(jsonPath("$.tags").isEmpty());

        mvc.perform(delete("/api/notes/{id}", id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/notes/{id}", id)).andExpect(status().isNotFound());
    }

    private long create(String title, String content, String tag) throws Exception {
        String body = mvc.perform(post("/api/notes").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "%s", "content": "%s", "tags": ["%s"]}
                        """.formatted(title, content, tag)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));
    }
}
