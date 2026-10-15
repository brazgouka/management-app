package com.management.timer;

import com.management.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class TimeEntryApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void startStopLifecycle() throws Exception {
        mvc.perform(get("/api/time-entries/current")).andExpect(status().isNoContent());

        String body = mvc.perform(post("/api/time-entries/start").contentType(MediaType.APPLICATION_JSON).content("""
                        {"activity": "Write README", "focus": "PRODUCTIVE"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.running").value(true))
                .andReturn().getResponse().getContentAsString();
        long id = Long.parseLong(body.replaceAll(".*\"id\":(\\d+).*", "$1"));

        mvc.perform(get("/api/time-entries/current"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activity").value("Write README"));

        mvc.perform(post("/api/time-entries/start").contentType(MediaType.APPLICATION_JSON).content("""
                        {"activity": "Doomscrolling", "focus": "PROCRASTINATION"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Conflict"));

        mvc.perform(post("/api/time-entries/{id}/stop", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.running").value(false))
                .andExpect(jsonPath("$.endedAt").exists());

        mvc.perform(post("/api/time-entries/{id}/stop", id)).andExpect(status().isConflict());
    }

    @Test
    void logsFinishedTimeAndReportsStats() throws Exception {
        mvc.perform(post("/api/time-entries").contentType(MediaType.APPLICATION_JSON).content("""
                        {"activity": "Study", "focus": "PRODUCTIVE", "startedAt": "2026-01-05T09:00:00Z", "endedAt": "2026-01-05T10:30:00Z"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.durationSeconds").value(5400));

        mvc.perform(post("/api/time-entries").contentType(MediaType.APPLICATION_JSON).content("""
                        {"activity": "Memes", "focus": "PROCRASTINATION", "startedAt": "2026-01-05T11:00:00Z", "endedAt": "2026-01-05T11:30:00Z"}
                        """))
                .andExpect(status().isCreated());

        mvc.perform(get("/api/time-entries/stats").param("from", "2026-01-05").param("to", "2026-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalSeconds").value(7200))
                .andExpect(jsonPath("$.secondsByFocus.PRODUCTIVE").value(5400))
                .andExpect(jsonPath("$.productivityScore").value(75))
                .andExpect(jsonPath("$.topActivities[0].activity").value("Study"));
    }

    @Test
    void stopUnknownEntryReturns404() throws Exception {
        mvc.perform(post("/api/time-entries/{id}/stop", 999_999)).andExpect(status().isNotFound());
    }
}
