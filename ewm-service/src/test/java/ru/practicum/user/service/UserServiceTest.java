package ru.practicum.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.extention.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.repository.UserRepository;
import ru.practicum.extention.ConditionsNotMetException;


import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto newUserRequest;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("Test User");
        user.setEmail("test@example.com");

        newUserRequest = new UserDto();
        newUserRequest.setName("Test User");
        newUserRequest.setEmail("test@example.com");

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("Test User");
        userDto.setEmail("test@example.com");
    }

    @Test
    void getAllWhenNoIdsThenReturnAllUsers() {
        when(userRepository.findUsers(null, 0, 10)).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll(null, 0, 10);

        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.getFirst().getId());
        assertEquals(userDto.getName(), result.getFirst().getName());
        assertEquals(userDto.getEmail(), result.getFirst().getEmail());

        verify(userRepository).findUsers(null, 0, 10);
    }

    @Test
    void getAllWhenWithIdsThenReturnFilteredUsers() {
        List<Long> ids = List.of(1L);
        when(userRepository.findUsers(ids, 0, 10)).thenReturn(List.of(user));

        List<UserDto> result = userService.getAll(ids, 0, 10);

        assertEquals(1, result.size());
        assertEquals(userDto.getId(), result.getFirst().getId());

        verify(userRepository).findUsers(ids, 0, 10);
    }

    @Test
    void createWhenValidRequestThenReturnCreatedUser() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(newUserRequest);

        assertNotNull(result);
        assertEquals(userDto.getId(), result.getId());
        assertEquals(userDto.getName(), result.getName());
        assertEquals(userDto.getEmail(), result.getEmail());

        verify(userRepository).findByEmail(newUserRequest.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createWhenEmailExistsThenThrowConditionsNotMetException() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ConditionsNotMetException exception = assertThrows(
                ConditionsNotMetException.class,
                () -> userService.create(newUserRequest)
        );

        assertEquals("Пользователь с таким email уже существует", exception.getMessage());
        verify(userRepository).findByEmail(newUserRequest.getEmail());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteWhenUserExistsThenDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).deleteById(1L);

        userService.delete(1L);

        verify(userRepository).findById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteWhenUserNotExistsThenThrowNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> userService.delete(1L)
        );

        assertEquals("Пользователь с id=1 не найден", exception.getMessage());
        verify(userRepository).findById(1L);
        verify(userRepository, never()).deleteById(anyLong());
    }
}