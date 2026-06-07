package ru.aston.hometask.stage2_4.service;

import ru.aston.hometask.stage2_4.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO create(UserDTO dto);
    List<UserDTO> getAll();
    UserDTO getById(Long id);
    UserDTO update(Long id, UserDTO dto);
    void delete(Long id);
}