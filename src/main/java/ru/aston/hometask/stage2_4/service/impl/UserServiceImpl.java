package ru.aston.hometask.stage2_4.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.aston.hometask.stage2_4.dao.UserRepository;
import ru.aston.hometask.stage2_4.dto.UserDTO;
import ru.aston.hometask.stage2_4.exception.BadRequestException;
import ru.aston.hometask.stage2_4.exception.ResourceNotFoundException;
import ru.aston.hometask.stage2_4.mapper.UserMapper;
import ru.aston.hometask.stage2_4.model.User;
import ru.aston.hometask.stage2_4.service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.aston.hometask.stage2_4.mapper.UserMapper.toUserDTO;
import static ru.aston.hometask.stage2_4.mapper.UserMapper.toUserEntity;

@Service
public class UserServiceImpl implements UserService {
    private static final String USER_NOT_FOUND_MSG = "User not found: %d";
    private static final String EMAIL_DUPLICATE_MSG = "User email already exists: %s";

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional
    public UserDTO create(UserDTO dto) {
        if (userRepository.findByEmail(dto.email()).isPresent()) {
            throw new BadRequestException(String.format(EMAIL_DUPLICATE_MSG, dto.email()));
        }
        User user = toUserEntity(dto);
        return toUserDTO(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDTO> getAll() {
        return userRepository.findAll()
                .stream()
                .map(UserMapper::toUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, id)));
        return toUserDTO(user);
    }

    @Override
    @Transactional
    public UserDTO update(Long id, UserDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, id)));
        Optional<User> userByEmail = userRepository.findByEmail(dto.email());

        if (userByEmail.isPresent() && !userByEmail.get().getId().equals(user.getId())) {
            throw new BadRequestException(String.format(EMAIL_DUPLICATE_MSG, dto.email()));
        }

        User updatedUser = toUserEntity(dto);
        updatedUser.setId(id);
        return toUserDTO(userRepository.save(updatedUser));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, id));
        }
        userRepository.deleteById(id);
    }
}