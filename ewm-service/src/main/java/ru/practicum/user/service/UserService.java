package ru.practicum.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.extention.ConditionsNotMetException;
import ru.practicum.extention.NotFoundException;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserDto;
import ru.practicum.user.model.UserMapper;
import ru.practicum.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        return userRepository.findUsers(ids, from, size).stream()
                .map(UserMapper::toUserDto)
                .toList();
    }

    public UserDto create(UserDto user) {
        Optional<User> userByEmail = userRepository.findByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            throw new ConditionsNotMetException("Пользователь с таким email уже существует");
        }
        return UserMapper.toUserDto(
                userRepository.save(UserMapper.toUser(user))
        );
    }

    public void delete(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + userId + " не найден")
        );
        userRepository.deleteById(userId);
    }
}
