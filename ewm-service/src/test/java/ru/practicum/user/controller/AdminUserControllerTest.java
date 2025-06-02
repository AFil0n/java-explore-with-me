package ru.practicum.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.extention.NotFoundException;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.service.UserService;


import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private UserDto userDto;
    private UserDto newUserRequest;

    @BeforeEach
    void setUp() {
        userDto = UserDto.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .build();

        newUserRequest = UserDto.builder()
                .name("Test User")
                .email("test@example.com")
                .build();
    }

    @Test
    void getAllUsersWhenNoIdsThenReturnAllUsers() throws Exception {
        List<UserDto> users = List.of(userDto);
        Mockito.when(userService.getAll(null, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()))
                .andExpect(jsonPath("$[0].name").value(userDto.getName()))
                .andExpect(jsonPath("$[0].email").value(userDto.getEmail()));

        Mockito.verify(userService).getAll(null, 0, 10);
    }

    @Test
    void getAllUsersWhenWithIdsThenReturnFilteredUsers() throws Exception {
        List<Long> ids = List.of(1L, 2L);
        List<UserDto> users = List.of(userDto);
        Mockito.when(userService.getAll(ids, 0, 10)).thenReturn(users);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1,2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(userDto.getId()));

        Mockito.verify(userService).getAll(ids, 0, 10);
    }

    @Test
    void getAllUsersWhenInvalidFromParamThenBadRequest() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("from", "-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllUsersWhenInvalidSizeParamThenBadRequest() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("size", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createUserWhenValidRequestThenCreated() throws Exception {
        Mockito.when(userService.create(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(userDto.getId()))
                .andExpect(jsonPath("$.name").value(userDto.getName()))
                .andExpect(jsonPath("$.email").value(userDto.getEmail()));

        Mockito.verify(userService).create(any(UserDto.class));
    }

    @Test
    void createUserWhenInvalidRequestThenBadRequest() throws Exception {
        UserDto invalidRequest = UserDto.builder().build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUserWhenValidIdThenNoContent() throws Exception {
        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(userService).delete(1L);
    }

    @Test
    void deleteUserWhenUserNotFoundThenNotFound() throws Exception {
        Long nonExistentUserId = 999L;
        doThrow(new NotFoundException("User not found"))
                .when(userService).delete(nonExistentUserId);

        mockMvc.perform(delete("/admin/users/{userId}", nonExistentUserId))
                .andExpect(status().isNotFound());

        Mockito.verify(userService).delete(nonExistentUserId);
    }
}
