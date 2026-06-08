package ru.aston.hometask.stage2_4.service;

import ru.aston.hometask.stage2_4.dto.UserDTO;

import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDTO create(UserDTO dto);
    List<UserDTO> getAll();
    UserDTO getById(UUID id);
    UserDTO update(UUID id, UserDTO dto);
    void delete(UUID id);
}