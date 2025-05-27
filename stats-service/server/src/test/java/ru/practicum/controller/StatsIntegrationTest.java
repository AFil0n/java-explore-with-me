package ru.practicum.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.utils.SimpleDateTimeFormatter;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:clean-up.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class StatsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StatsRepository repository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Test
    void hitAndGetStatsShouldWorkTogether() throws Exception {
        long initialCount = repository.count();

        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("integration-test-app");
        hitDto.setUri("/integration-test");
        hitDto.setIp("127.0.0.1");
        hitDto.setTimestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()));

        mockMvc.perform(post("/hit")
                        .contentType("application/json")
                        .content("{\"app\":\"integration-test-app\",\"uri\":\"/integration-test\"," +
                                "\"ip\":\"127.0.0.1\",\"timestamp\":\"" + LocalDateTime.now().format(formatter) + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.app").value("integration-test-app"))
                .andExpect(jsonPath("$.uri").value("/integration-test"));

        assertEquals(initialCount + 1, repository.count());

        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter))
                        .param("uris", "/integration-test") // Фильтруем только наш тестовый URI
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].app").value("integration-test-app"))
                .andExpect(jsonPath("$[0].uri").value("/integration-test"))
                .andExpect(jsonPath("$[0].hits").value(1));
    }

    @Test
    void getStatsWithTestDataShouldReturnCorrectResults() throws Exception {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 1, 3, 0, 0);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter))
                        .param("uris", "/events/1", "/events/2")
                        .param("unique", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].hits").value(3))
                .andExpect(jsonPath("$[1].hits").value(2));

        mockMvc.perform(get("/stats")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter))
                        .param("uris", "/events/1", "/events/2")
                        .param("unique", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].hits").value(2))
                .andExpect(jsonPath("$[1].hits").value(1));
    }

    @Test
    void getStatsWithInvalidDatesShouldReturnBadRequest() throws Exception {
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = LocalDateTime.now().minusDays(1);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(formatter))
                        .param("end", end.format(formatter)))
                .andExpect(status().isBadRequest());
    }
}