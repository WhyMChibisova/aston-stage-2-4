package ru.aston.hometask.stage2_4.mapper;

import ru.aston.hometask.stage2_4.dto.UserDTO;
import ru.aston.hometask.stage2_4.model.User;

public class UserMapper {
    public static User toUserEntity(UserDTO dto) {
        return User.builder()
                .name(dto.name())
                .email(dto.email())
                .age(dto.age())
                .build();
    }

    public static UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .name(user.getName())
                .email(user.getEmail())
                .age(user.getAge())
                .build();
    }
}