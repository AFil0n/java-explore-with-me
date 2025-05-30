package ru.practicum.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import ru.practicum.extention.ExceptionMessages;
import ru.practicum.extention.OptionalParams;

@Data
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;

    @NotBlank
    @Email
    @Size(min = OptionalParams.MinUserEmailSize, message = ExceptionMessages.UserEmailMinLenghtError)
    @Size(max = OptionalParams.MaxUserEmailSize, message = ExceptionMessages.UserEmailMaxLenghtError)
    String email;

    @NotBlank
    @Size(min = OptionalParams.MinUserNameSize, message = ExceptionMessages.UserNameMinLenghtError)
    @Size(max = OptionalParams.MaxUserNameSize, message = ExceptionMessages.UserNameMaxLenghtError)
    String name;
}
