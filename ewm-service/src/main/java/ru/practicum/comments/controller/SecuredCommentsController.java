package ru.practicum.comments.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comments.model.CommentDto;
import ru.practicum.comments.model.CommentUpdateDto;
import ru.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/users/comments")
@Validated
public class SecuredCommentsController {
    private final CommentService commentService;

    @PostMapping("/{userId}/{eventId}")
    @ResponseStatus(value = HttpStatus.CREATED)
    public CommentDto addComment(@Valid @RequestBody CommentDto commentNewDto,
                                 @PathVariable Long userId,
                                 @PathVariable Long eventId) {

        return commentService.addComment(userId, eventId, commentNewDto);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    public CommentDto updateComment(@Valid @RequestBody CommentUpdateDto commentUpdateDto,
                                    @PathVariable Long userId) {

        return commentService.updateComment(userId, commentUpdateDto);
    }

    @DeleteMapping("/{userId}/{commentId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable Long userId,
                              @PathVariable Long commentId) {

        commentService.deletePrivateComment(userId, commentId);
    }

    @GetMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.OK)
    public List<CommentDto> getCommentsByUserId(@PathVariable Long userId,
                                                @RequestParam(required = false, name = "rangeStart") String rangeStart,
                                                @RequestParam(required = false, name = "rangeEnd") String rangeEnd,
                                                @PositiveOrZero @RequestParam(name = "from", defaultValue = "0") Integer from,
                                                @Positive @RequestParam(name = "size", defaultValue = "10") Integer size) {

        return commentService.getCommentsByUserId(rangeStart, rangeEnd, userId, from, size);
    }
}
