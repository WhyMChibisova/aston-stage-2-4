package ru.aston.hometask.stage2_4.service;

import ru.aston.hometask.stage2_4.dto.UserDto;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto create(UserDto dto);
    List<UserDto> getAll();
    UserDto getById(UUID id);
    UserDto update(UUID id, UserDto dto);
    void delete(UUID id);
}