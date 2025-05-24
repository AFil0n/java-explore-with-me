package ru.practicum;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.practicum.dto.EndpointHitDto;
import ru.practicum.utils.SimpleDateTimeFormatter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatsClientTest {

    @Mock
    private RestTemplate restTemplate;

    private StatsClient statsClient;

    @BeforeEach
    void setUp() {
        RestTemplateBuilder builder = mock(RestTemplateBuilder.class);
        when(builder.build()).thenReturn(restTemplate);
        when(builder.uriTemplateHandler(any())).thenReturn(builder);
        when(builder.requestFactory(any(Supplier.class))).thenReturn(builder);

        String serverUrl = "http://localhost:9090";
        statsClient = new StatsClient(serverUrl, builder);
    }

    @Test
    void saveShouldCallPostRequest() {
        EndpointHitDto hitDto = new EndpointHitDto();
        hitDto.setApp("test-app");
        hitDto.setUri("/test");
        hitDto.setIp("127.0.0.1");
        hitDto.setTimestamp(SimpleDateTimeFormatter.toString(LocalDateTime.now()));

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(this::checkHeaders),
                eq(Object.class))
        ).thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = statsClient.save(hitDto);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.POST),
                argThat(this::checkHeaders),
                eq(Object.class));
    }

    @Test
    void getStatsShouldCallGetRequestWithParameters() {
        String start = "2023-01-01 00:00:00";
        String end = "2023-01-02 00:00:00";
        List<String> uris = List.of("/test1", "/test2");
        Boolean unique = true;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::checkHeaders),
                eq(Object.class),
                eq(Map.of("start", start, "end", end, "uris", uris, "unique", unique)))
        ).thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = statsClient.getStats(start, end, uris, unique);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::checkHeaders),
                eq(Object.class),
                eq(Map.of("start", start, "end", end, "uris", uris, "unique", unique)));
    }

    @Test
    void getStatsWithEmptyUrisShouldCallGetRequestWithoutUris() {
        String start = "2023-01-01 00:00:00";
        String end = "2023-01-02 00:00:00";
        List<String> uris = List.of();
        Boolean unique = false;

        ResponseEntity<Object> expectedResponse = ResponseEntity.ok().build();
        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::checkHeaders),
                eq(Object.class),
                eq(Map.of("start", start, "end", end, "uris", uris, "unique", unique)))
        ).thenReturn(expectedResponse);

        ResponseEntity<Object> actualResponse = statsClient.getStats(start, end, uris, unique);

        assertNotNull(actualResponse);
        assertEquals(expectedResponse.getStatusCode(), actualResponse.getStatusCode());
        verify(restTemplate, times(1)).exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::checkHeaders),
                eq(Object.class),
                eq(Map.of("start", start, "end", end, "uris", uris, "unique", unique)));
    }

    @Test
    void getStatsWhenServerErrorShouldReturnErrorResponse() {
        String start = "2023-01-01 00:00:00";
        String end = "2023-01-02 00:00:00";
        List<String> uris = List.of("/test");
        Boolean unique = true;

        HttpStatusCodeException exception = mock(HttpStatusCodeException.class);
        when(exception.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR);
        when(exception.getResponseBodyAsByteArray()).thenReturn("Server error".getBytes());

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                argThat(this::checkHeaders),
                eq(Object.class),
                eq(Map.of("start", start, "end", end, "uris", uris, "unique", unique)))
        ).thenThrow(exception);

        ResponseEntity<Object> response = statsClient.getStats(start, end, uris, unique);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    private boolean checkHeaders(HttpEntity<?> entity) {
        HttpHeaders headers = entity.getHeaders();
        return Objects.equals(headers.getContentType(), MediaType.APPLICATION_JSON) &&
                headers.getAccept().contains(MediaType.APPLICATION_JSON);
    }
}

