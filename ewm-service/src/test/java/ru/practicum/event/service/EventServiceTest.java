package ru.practicum.event.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.StatsClient;
import ru.practicum.category.model.Category;
import ru.practicum.category.repository.CategoryRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventAdminStateAction;
import ru.practicum.event.model.EventDto;
import ru.practicum.event.model.EventSearchAdmin;
import ru.practicum.event.model.EventSearchCommon;
import ru.practicum.event.model.EventState;
import ru.practicum.event.model.EventUserStateAction;
import ru.practicum.event.model.Location;
import ru.practicum.event.model.UpdateAdminEventDto;
import ru.practicum.event.model.UpdateEventDto;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.DateValidationException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserMapper;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;


    @Mock
    private StatsClient statsClient;

    @Mock
    private UserService userService;

    @InjectMocks
    private EventService eventService;

    private User user;
    private Category category;
    private Event event;
    private EventDto newEventDto;
    private UpdateAdminEventDto adminRequest;
    private UpdateEventDto userRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        category = new Category();
        category.setId(1L);
        category.setName("Test Category");

        event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");
        event.setAnnotation("Test Annotation");
        event.setDescription("Test Description");
        event.setEventDate(LocalDateTime.now().plusDays(1));
        event.setInitiator(user);
        event.setCategory(category);
        event.setPaid(false);
        event.setParticipantLimit(10L);
        event.setRequestModeration(true);
        event.setState(EventState.PENDING);
        event.setCreatedOn(LocalDateTime.now());
        event.setLat(55.754167);
        event.setLon(37.620000);

        newEventDto = new EventDto();
        newEventDto.setTitle("New Event");
        newEventDto.setAnnotation("New Annotation");
        newEventDto.setDescription("New Description");
        newEventDto.setEventDate(LocalDateTime.now().plusDays(2));
        newEventDto.setCategory(1L);
        newEventDto.setPaid(true);
        newEventDto.setParticipantLimit(20L);
        newEventDto.setRequestModeration(false);
        newEventDto.setLocation(new Location(55.755814, 37.617635));

        adminRequest = new UpdateAdminEventDto();
        adminRequest.setTitle("Updated Title");
        adminRequest.setStateAction(EventAdminStateAction.PUBLISH_EVENT);

        userRequest = new UpdateEventDto();
        userRequest.setTitle("User Updated Title");
        userRequest.setStateAction(EventUserStateAction.SEND_TO_REVIEW);
    }

    @Test
    void findByUserIdShouldReturnEventShortDtoList() {
        when(eventRepository.findAllByInitiatorId(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of(event));

        List<EventDto> result = eventService.findByUserId(1L, 0, 10);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.getFirst().getTitle());
        verify(eventRepository).findAllByInitiatorId(1L, 0, 10);
    }

    @Test
    void findByIdAndUserWhenValidShouldReturnEventFullDto() {
        when(userService.findUserById(1L)).thenReturn(user);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        EventDto result = eventService.findByIdAndUser(1L, 1L);

        assertEquals(event.getTitle(), result.getTitle());
        verify(userService).findUserById(1L);
        verify(eventRepository).findById(1L);
    }

    @Test
    void findByIdAndUserWhenUserNotExistsShouldThrowNotFoundException() {
        when(userService.findUserById(1L))
                .thenThrow(new NotFoundException("Пользователь не найден"));

        assertThrows(NotFoundException.class, () -> eventService.findByIdAndUser(1L, 1L));
        verify(userService).findUserById(1L);
        verify(eventRepository, never()).findById(anyLong());
    }

    @Test
    void findByIdAndUserWhenEventNotBelongsToUserShouldThrowConditionsNotMetException() {
        when(userService.findUserById(1L)).thenReturn(user);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        user.setId(2L); // Different user

        assertThrows(ConditionsNotMetException.class, () -> eventService.findByIdAndUser(1L, 1L));
    }

    @Test
    void searchCommonShouldReturnEventShortDtoList() {
        EventSearchCommon search = new EventSearchCommon();
        when(eventRepository.findCommonEventsByFilters(search)).thenReturn(List.of(event));

        List<EventDto> result = eventService.searchCommon(search);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.getFirst().getTitle());
        verify(eventRepository).findCommonEventsByFilters(search);
    }

    @Test
    void searchCommonWhenInvalidDateRangeShouldThrowDateValidationException() {
        EventSearchCommon search = new EventSearchCommon();
        search.setRangeStart(LocalDateTime.now().plusDays(1));
        search.setRangeEnd(LocalDateTime.now());

        assertThrows(DateValidationException.class, () -> eventService.searchCommon(search));
        verify(eventRepository, never()).findCommonEventsByFilters(any());
    }

    @Test
    void searchAdminShouldReturnEventFullDtoList() {
        EventSearchAdmin search = new EventSearchAdmin();
        when(eventRepository.findAdminEventsByFilters(search)).thenReturn(List.of(event));

        List<EventDto> result = eventService.searchAdmin(search);

        assertEquals(1, result.size());
        assertEquals(event.getTitle(), result.getFirst().getTitle());
        verify(eventRepository).findAdminEventsByFilters(search);
    }

    @Test
    void findByIdWhenPublishedShouldReturnEventFullDto() {
        event.setState(EventState.PUBLISHED);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(statsClient.getStats(anyString(), anyString(), anyList(), anyBoolean()))
                .thenReturn(List.of());

        EventDto result = eventService.findById(1L);

        assertEquals(event.getTitle(), result.getTitle());
        verify(eventRepository).findById(1L);
        verify(statsClient).getStats(anyString(), anyString(), anyList(), anyBoolean());
    }

    @Test
    void findByIdWhenNotPublishedShouldThrowNotFoundException() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertThrows(NotFoundException.class, () -> eventService.findById(1L));
    }

    @Test
    void createWhenValidShouldReturnEventFullDto() {
        when(userService.findUserById(1L)).thenReturn(user);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventDto result = eventService.create(1L, newEventDto);

        assertEquals(event.getTitle(), result.getTitle());
        verify(userService).findUserById(1L);
        verify(categoryRepository).findById(1L);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void createWhenEventDateTooEarlyShouldThrowDateValidationException() {
        newEventDto.setEventDate(LocalDateTime.now().plusHours(1));
        when(userService.findUserById(1L)).thenReturn(user);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThrows(DateValidationException.class, () -> eventService.create(1L, newEventDto));
        verify(eventRepository, never()).save(any(Event.class));
    }

    @Test
    void updateByAdminWhenValidShouldReturnUpdatedEvent() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventDto result = eventService.updateByAdmin(1L, adminRequest);

        assertEquals(adminRequest.getTitle(), result.getTitle());
        assertEquals(EventState.PUBLISHED, event.getState());
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(event);
    }

    @Test
    void updateByAdminWhenCategoryNotExistsShouldThrowNotFoundException() {
        adminRequest.setCategory(999L);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByAdminWhenRejectPublishedEventShouldThrowConditionsNotMetException() {
        event.setState(EventState.PUBLISHED);
        adminRequest.setStateAction(EventAdminStateAction.REJECT_EVENT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Опубликованное событие нельзя отклонить.", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByAdminWhenPublishNotPendingEventShouldThrowConditionsNotMetException() {
        event.setState(EventState.CANCELED);
        adminRequest.setStateAction(EventAdminStateAction.PUBLISH_EVENT);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Опубликовать можно только событие в состоянии ожидания.", exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByAdminWhenEventDateTooEarlyShouldThrowDateValidationException() {
        adminRequest.setEventDate(LocalDateTime.now().plusMinutes(30));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        DateValidationException exception = assertThrows(
                DateValidationException.class,
                () -> eventService.updateByAdmin(1L, adminRequest)
        );

        assertEquals("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.",
                exception.getMessage());
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenValidShouldReturnUpdatedEvent() {
        when(userService.findUserById(1L)).thenReturn(user);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventDto result = eventService.updateByUser(1L, 1L, userRequest);

        assertEquals(userRequest.getTitle(), result.getTitle());
        assertEquals(EventState.PENDING, event.getState());
        verify(userService).findUserById(1L);
        verify(eventRepository).findById(1L);
        verify(eventRepository).save(event);
    }

    @Test
    void updateByUserWhenCategoryNotExistsShouldThrowNotFoundException() {
        userRequest.setCategory(999L);
        when(userService.findUserById(1L)).thenReturn(user);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> eventService.updateByUser(1L, 1L, userRequest)
        );

        assertEquals("Категория с id=999 не найдена", exception.getMessage());
        verify(userService).findUserById(1L);
        verify(eventRepository).findById(1L);
        verify(categoryRepository).findById(999L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenEventDateTooEarlyShouldThrowDateValidationException() {
        userRequest.setEventDate(LocalDateTime.now().plusMinutes(30));
        when(userService.findUserById(1L)).thenReturn(user);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        DateValidationException exception = assertThrows(
                DateValidationException.class,
                () -> eventService.updateByUser(1L, 1L, userRequest)
        );

        assertEquals("Дата начала события должна быть не ранее чем через 1 час от даты редактирования.",
                exception.getMessage());
        verify(userService).findUserById(1L);
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenUpdatePublishedEventShouldThrowConditionsNotMetException() {
        event.setState(EventState.PUBLISHED);
        when(userService.findUserById(1L)).thenReturn(user);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByUser(1L, 1L, userRequest)
        );

        assertEquals("Нельзя редактировать опубликованное событие", exception.getMessage());
        verify(userService).findUserById(1L);
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateByUserWhenNotInitiatorShouldThrowConditionsNotMetException() {
        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Other User");
        otherUser.setEmail("other@example.com");

        when(userService.findUserById(2L)).thenReturn(otherUser);
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

        assertNotEquals(2L, event.getInitiator().getId(),
                "Тест невалиден: event уже принадлежит пользователю с ID=2");

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> eventService.updateByUser(2L, 1L, userRequest)
        );

        assertEquals("Событие может редактировать только его создатель", exception.getMessage());
        verify(userService).findUserById(2L);
        verify(eventRepository).findById(1L);
        verify(eventRepository, never()).save(any());
    }
}
