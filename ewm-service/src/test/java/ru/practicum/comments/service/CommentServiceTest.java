package ru.practicum.comments.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import ru.practicum.category.model.Category;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentMapper;
import ru.practicum.comments.model.CommentDto;
import ru.practicum.comments.model.CommentUpdateDto;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.EventState;
import ru.practicum.event.service.EventService;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.DateValidationException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private UserService userService;

    @Mock
    private EventService eventService;

    @InjectMocks
    private CommentService commentService;

    private User user;
    private Category category;
    private Event event;
    private Comment comment;
    private CommentDto commentDto;
    private CommentUpdateDto commentUpdateDto;

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

        comment = Comment.builder()
                .id(1L)
                .user(user)
                .event(event)
                .message("Test comment")
                .created(LocalDateTime.now().minusDays(1))
                .build();

        commentDto = CommentMapper.toCommentDto(comment);

        commentUpdateDto = CommentUpdateDto.builder()
                .id(1L)
                .message("Updated comment")
                .build();
    }

    @Test
    void addComment_ShouldReturnSavedComment() {
        when(userService.findUserById(anyLong())).thenReturn(user);
        when(eventService.findEventById(anyLong())).thenReturn(event);
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.addComment(1L, 1L, commentDto);

        assertNotNull(result);
        assertEquals(commentDto.getMessage(), result.getMessage());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    void updateComment_ShouldUpdateCommentWhenUserIsOwner() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment savedComment = invocation.getArgument(0);
            return savedComment;
        });

        CommentDto result = commentService.updateComment(1L, commentUpdateDto);

        assertNotNull(result);
        assertEquals("Updated comment", result.getMessage());
    }

    @Test
    void updateComment_ShouldThrowExceptionWhenUserNotOwner() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        assertThrows(ConditionsNotMetException.class,
                () -> commentService.updateComment(2L, commentUpdateDto));
    }

    @Test
    void deletePrivateComment_ShouldDeleteWhenUserIsOwner() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
        when(userService.findUserById(anyLong())).thenReturn(user);

        commentService.deletePrivateComment(1L, 1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void getComments_ShouldReturnAllComments() {
        when(commentRepository.getComments(any(), any(), any(Pageable.class)))
                .thenReturn(List.of(comment));

        List<CommentDto> result = commentService.getComments(null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getCommentsByEventId_ShouldReturnEventComments() {
        when(eventService.findEventById(anyLong())).thenReturn(event);
        when(commentRepository.getCommentsByEventId(anyLong(), any(), any(), any(Pageable.class)))
                .thenReturn(List.of(comment));

        List<CommentDto> result = commentService.getCommentsByEventId(null, null, 1L, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void deleteAdminComment_ShouldDeleteWithoutOwnershipCheck() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        commentService.deleteAdminComment(1L);

        verify(commentRepository, times(1)).deleteById(1L);
    }

    @Test
    void findById_ShouldReturnComment() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));

        CommentDto result = commentService.findById(1L);

        assertNotNull(result);
        assertEquals(commentDto.getId(), result.getId());
    }

    @Test
    void findById_ShouldThrowWhenNotFound() {
        when(commentRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> commentService.findById(1L));
    }

    @Test
    void getDateRange_ShouldValidateDatesCorrectly() {
        String start = "2022-01-01 00:00:00";
        String end = "2022-01-02 00:00:00";
        assertDoesNotThrow(() -> commentService.getDateRange(start, end));

        String invalidStart = "2022-01-03 00:00:00";
        assertThrows(DateValidationException.class, () -> commentService.getDateRange(invalidStart, end));
    }

    @Test
    void verifyCommentOwnership_ShouldThrowWhenNotOwner() {
        Comment commentWithDifferentUser = new Comment();
        User otherUser = new User();
        otherUser.setId(2L);
        commentWithDifferentUser.setUser(otherUser);

        assertThrows(ConditionsNotMetException.class,
                () -> commentService.verifyCommentOwnership(commentWithDifferentUser, 1L));
    }

    @Test
    void getPageable_ShouldReturnCorrectPageable() {
        // Test with pagination
        Pageable pageable = commentService.getPageable(10, 5);
        assertNotNull(pageable);
        assertEquals(2, pageable.getPageNumber());

        // Test without pagination
        Pageable unpaged = commentService.getPageable(null, null);
        assertTrue(unpaged.isUnpaged());
    }
}
