package ru.practicum.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.model.ParticipationRequestStatus;
import ru.practicum.request.repository.ParticipationRequestRepository;
import ru.practicum.user.model.User;
import ru.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class ParticipationRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ParticipationRequestRepository requestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private User user;
    private Event event;
    private ParticipationRequest request;
    private Category category;

    @BeforeEach
    void setUp() {
        requestRepository.deleteAll();
        eventRepository.deleteAll();
        userRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Test Category")
                .build());

        user = userRepository.save(User.builder()
                .name("User")
                .email("user@example.com")
                .build());

        User initiator = userRepository.save(User.builder()
                .name("Initiator")
                .email("initiator@example.com")
                .build());

        event = eventRepository.save(Event.builder()
                .title("Test Event")
                .annotation("Test Annotation")
                .description("Test Description")
                .eventDate(LocalDateTime.now().plusDays(1))
                .initiator(initiator)
                .category(category)  // Добавляем категорию
                .state(EventState.PUBLISHED)
                .participantLimit(10L)
                .confirmedRequests(0L)
                .requestModeration(true)
                .lat(55.754167)
                .lon(37.620000)
                .paid(false)
                .createdOn(LocalDateTime.now())
                .build());

        request = requestRepository.save(ParticipationRequest.builder()
                .requester(user)
                .event(event)
                .status(ParticipationRequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build());
    }

    @Test
    void createRequestShouldCreateAndReturnRequest() throws Exception {
        Event newEvent = eventRepository.save(Event.builder()
                .title("New Event")
                .annotation("New Annotation")
                .description("New Description")
                .eventDate(LocalDateTime.now().plusDays(2))
                .initiator(event.getInitiator())
                .category(category)
                .state(EventState.PUBLISHED)
                .participantLimit(5L)
                .confirmedRequests(0L)
                .requestModeration(true)
                .lat(55.755814)
                .lon(37.617635)
                .paid(true)
                .createdOn(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", user.getId(), newEvent.getId()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status").value("PENDING"));

        List<ParticipationRequest> requests = requestRepository.findAll();
        assertEquals(2, requests.size());
    }

    @Test
    void getAllRequestsShouldReturnUserRequests() throws Exception {
        mockMvc.perform(get("/users/{userId}/requests", user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(request.getId()));
    }

    @Test
    void cancelRequestShouldCancelAndReturnRequest() throws Exception {
        mockMvc.perform(patch("/users/{userId}/requests/{requestId}/cancel", user.getId(), request.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));

        ParticipationRequest canceledRequest = requestRepository.findById(request.getId()).orElseThrow();
        assertEquals(ParticipationRequestStatus.CANCELED, canceledRequest.getStatus());
    }

    @Test
    void createRequestWhenEventNotPublishedShouldReturnConflict() throws Exception {
        Event unpublishedEvent = eventRepository.save(Event.builder()
                .title("Unpublished Event")
                .annotation("Unpublished Annotation")
                .description("Unpublished Description")
                .eventDate(LocalDateTime.now().plusDays(3))
                .initiator(event.getInitiator())
                .category(category)  // Добавляем категорию
                .state(EventState.PENDING)
                .participantLimit(5L)
                .confirmedRequests(0L)
                .requestModeration(true)
                .lat(55.755814)
                .lon(37.617635)
                .paid(true)
                .createdOn(LocalDateTime.now())
                .build());

        mockMvc.perform(post("/users/{userId}/requests?eventId={eventId}", user.getId(), unpublishedEvent.getId()))
                .andExpect(status().isConflict());
    }
}
