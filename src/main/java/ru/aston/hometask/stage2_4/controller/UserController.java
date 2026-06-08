package ru.aston.hometask.stage2_4.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.aston.hometask.stage2_4.dto.UserDTO;
import ru.aston.hometask.stage2_4.service.UserService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public UserDTO create(@Valid @RequestBody UserDTO dto) {
        return userService.create(dto);
    }

    @GetMapping
    public List<UserDTO> getAll() {
        return userService.getAll();
    }

    @GetMapping("/{id}")
    public UserDTO getById(@PathVariable UUID id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    public UserDTO update(@PathVariable UUID id, @Valid @RequestBody UserDTO dto) {
        return userService.update(id, dto);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }
}