package ru.practicum.user.model;

public class UserMapper {
    public static User toUser(UserDto userDto) {
        return User.builder()
                .id(userDto.getId())
                .email(userDto.getEmail())
                .name(userDto.getName())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public static UserDto toUserDtoWithoutEmail(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }
}
