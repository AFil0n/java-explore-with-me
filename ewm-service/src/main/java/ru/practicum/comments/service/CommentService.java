package ru.practicum.comments.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.model.CommentDto;
import ru.practicum.comments.model.CommentUpdateDto;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.DateValidationException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.comments.model.CommentMapper;
import ru.practicum.user.service.UserService;
import ru.practicum.utils.SimpleDateTimeFormatter;

import static ru.practicum.utils.SimpleDateTimeFormatter.CURRENT_TIME;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserService userService;
    private final EventService eventService;

    @Transactional
    public CommentDto addComment(Long userId, Long eventId, CommentDto commentNewDto) {
        User user = userService.findUserById(userId);
        Event event = eventService.findEventById(eventId);
        return CommentMapper.toCommentDto(commentRepository.save(CommentMapper.toComment(commentNewDto, user, event)));
    }

    @Transactional
    public CommentDto updateComment(Long userId, CommentUpdateDto commentUpdateDto) {
        Comment comment = findCommentById(commentUpdateDto.getId());
        verifyCommentOwnership(comment, userId);
        comment.setMessage(commentUpdateDto.getMessage());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Transactional
    public void deletePrivateComment(Long userId, Long commentId) {
        userService.findUserById(userId);
        verifyCommentOwnership(findCommentById(commentId), userId);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public List<CommentDto> getCommentsByUserId(String rangeStart, String rangeEnd, Long userId, Integer from, Integer size) {
        userService.findById(userId);
        Map<String, LocalDateTime> dateRange = getDateRange(rangeStart, rangeEnd);
        List<Comment> comments = commentRepository.getCommentsByUserId(userId, dateRange.get("startTime"), dateRange.get("endTime"), getPageable(from, size));
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .toList();
    }

    @Transactional
    public List<CommentDto> getComments(String rangeStart, String rangeEnd, Integer from, Integer size) {
        Map<String, LocalDateTime> dateRange = getDateRange(rangeStart, rangeEnd);
        List<Comment> commentList = commentRepository.getComments(dateRange.get("startTime"), dateRange.get("endTime"), getPageable(from, size));
        return CommentMapper.toCommentDtoList(commentList);
    }

    @Transactional
    public void deleteAdminComment(Long commentId) {
        findCommentById(commentId);
        commentRepository.deleteById(commentId);
    }

    @Transactional
    public List<CommentDto> getCommentsByEventId(String rangeStart, String rangeEnd, Long eventId, Integer from, Integer size) {
        eventService.findEventById(eventId);
        Map<String, LocalDateTime> dateRange = getDateRange(rangeStart, rangeEnd);
        List<Comment> commentList = commentRepository.getCommentsByEventId(eventId, dateRange.get("startTime"), dateRange.get("endTime"), getPageable(from, size));
        return CommentMapper.toCommentDtoList(commentList);
    }

    public CommentDto findById(Long commentId) {
        return CommentMapper.toCommentDto(findCommentById(commentId));
    }

    public Comment findCommentById(Long commentId) {
        return commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("Комментария с id " + commentId + " не существует."));
    }

    public void verifyCommentOwnership(Comment comment, Long userId) {
        if (!userId.equals(comment.getUser().getId())) {
            throw new ConditionsNotMetException("Пользователь с id " + userId + " не является автором комментария с id " + comment.getId() + ".");
        }
    }

    public Map<String, LocalDateTime> getDateRange(String startTime, String endTime) {
        Map<String, LocalDateTime> dateRange = new HashMap<>();
        dateRange.put("startTime", parseDate(startTime));
        dateRange.put("endTime", parseDate(endTime));

        if (startTime != null && endTime != null) {
            if (dateRange.get("startTime").isAfter(dateRange.get("endTime"))) {
                throw new DateValidationException("Дата начала должна быть после End.");
            }
            if (dateRange.get("endTime").isAfter(CURRENT_TIME) || dateRange.get("startTime").isAfter(CURRENT_TIME)) {
                throw new DateValidationException("Дата конца должна должна быть в прошлом.");
            }
        }

        return dateRange;
    }

    public LocalDateTime parseDate(String date) {
        return date != null ? SimpleDateTimeFormatter.parse(date) : null;
    }

    public Pageable getPageable(Integer from, Integer size) {
        Pageable pageable = Pageable.unpaged();

        if (from != null && size != null) {
            pageable = Pageable.ofSize(size).withPage(from / size);
        }

        return pageable;
    }
}
