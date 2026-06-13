package ru.aston.hometask.stage2_4.mapper;

import ru.aston.hometask.stage2_4.dto.UserDto;
import ru.aston.hometask.stage2_4.model.User;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {
    public static User toUserEntity(UserDto dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .age(dto.age())
                .build();
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .build();
    }

    public static List<UserDto> toListUserDto(List<User> users) {
        if (users == null) {
            return Collections.emptyList();
        }
        return users.stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }
}