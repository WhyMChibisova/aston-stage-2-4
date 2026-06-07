package ru.aston.hometask.stage2_4.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.aston.hometask.stage2_4.dto.UserDTO;
import ru.aston.hometask.stage2_4.exception.BadRequestException;
import ru.aston.hometask.stage2_4.exception.ResourceNotFoundException;
import ru.aston.hometask.stage2_4.service.UserService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(UserController.class)
class UserControllerTest {
    private static final String END_POINT = "/api/users";
    private static final String USER_NOT_FOUND_MSG = "User not found: %d";
    private static final String EMAIL_DUPLICATE_MSG = "User email already exists: %s";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper mapper;

    @MockitoBean
    private UserService userService;

    private UserDTO userDTO;

    @BeforeEach
    void setUp() {
        userDTO = UserDTO.builder()
                .name("Masha")
                .email("test@test.com")
                .age(23)
                .build();
    }

    @Test
    void create_whenDataIsValid() throws Exception {
        when(userService.create(any(UserDTO.class)))
                .thenReturn(userDTO);

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Masha"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.age").value(23));
    }

    @Test
    void create_whenSameEmailExists_thanBadRequest() throws Exception {
        when(userService.create(any(UserDTO.class)))
                .thenThrow(new BadRequestException(String.format(EMAIL_DUPLICATE_MSG, "test@test.com")));

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("email already exists")));
    }

    @Test
    void create_whenDataInvalid_thanValidationErrors() throws Exception {
        UserDTO invalidUser = UserDTO.builder()
                .name("")
                .email("email")
                .age(-1)
                .build();

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.age").exists());

        verify(userService, never()).create(any());
    }

    @ParameterizedTest
    @MethodSource("invalidSizeNameAndEmail")
    void create_whenInvalidSizeNameAndEmail_thanValidationErrors(String name, String email, String errorField) throws Exception {
        UserDTO invalidUser = UserDTO.builder()
                .name(name)
                .email(email)
                .age(23)
                .build();

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors." + errorField).exists());
    }

    private static Stream<Arguments> invalidSizeNameAndEmail() {
        return Stream.of(
                Arguments.of("a".repeat(101), "test@test.com", "name"),
                Arguments.of("Masha", "a".repeat(101), "email")
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   " })
    void create_whenNameInvalid_thanValidationErrors(String name) throws Exception {
        UserDTO invalidUser = UserDTO.builder()
                .name(name)
                .email("test@test.com")
                .age(23)
                .build();

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = { " ", "   ", "test", "test@", "@test", "@test.com" })
    void create_whenEmailInvalid_thanValidationErrors(String email) throws Exception {
        UserDTO invalidUser = UserDTO.builder()
                .name("Masha")
                .email(email)
                .age(23)
                .build();

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(ints = { -1, 151 })
    void create_whenAgeInvalid_thanValidationErrors(Integer age) throws Exception {
        UserDTO invalidUser = UserDTO.builder()
                .name("Masha")
                .email("test@test.com")
                .age(age)
                .build();

        mockMvc.perform(post(END_POINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(invalidUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.age").exists());
    }

    @Test
    void getAll_whenUsersExist() throws Exception {
        List<UserDTO> users = List.of(
                userDTO,
                UserDTO.builder()
                        .name("Dima")
                        .email("test2@test.com")
                        .age(22)
                        .build()
        );

        when(userService.getAll())
                .thenReturn(users);

        mockMvc.perform(get(END_POINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Masha"))
                .andExpect(jsonPath("$[0].email").value("test@test.com"))
                .andExpect(jsonPath("$[0].age").value(23))
                .andExpect(jsonPath("$[1].name").value("Dima"))
                .andExpect(jsonPath("$[1].email").value("test2@test.com"))
                .andExpect(jsonPath("$[1].age").value(22));
    }

    @Test
    void getAll_whenUserNonExist_thenReturnEmpty() throws Exception {
        when(userService.getAll())
                .thenReturn(List.of());

        mockMvc.perform(get(END_POINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getById_whenUserExists() throws Exception {
        when(userService.getById(1L))
                .thenReturn(userDTO);

        mockMvc.perform(get(END_POINT + "/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Masha"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.age").value(23));
    }

    @Test
    void getById_whenUserNonExists_thanReturn404() throws Exception {
        when(userService.getById(1L))
                .thenThrow(new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, 1)));

        mockMvc.perform(get(END_POINT + "/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void update_whenUserIsValid() throws Exception {
        when(userService.update(eq(1L), any(UserDTO.class)))
                .thenReturn(userDTO);

        mockMvc.perform(put(END_POINT + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Masha"))
                .andExpect(jsonPath("$.email").value("test@test.com"))
                .andExpect(jsonPath("$.age").value(23));
    }

    @Test
    void update_whenSameEmailExists_thanBadRequest() throws Exception {
        when(userService.update(eq(1L), any(UserDTO.class)))
                .thenThrow(new BadRequestException(String.format(EMAIL_DUPLICATE_MSG, "test@test.com")));

        mockMvc.perform(put(END_POINT + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("email already exists")));
    }

    @Test
    void update_whenUserNonExists_thanReturn404() throws Exception {
        when(userService.update(eq(1L), any(UserDTO.class)))
                .thenThrow(new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, 1)));

        mockMvc.perform(put(END_POINT + "/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(userDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void deleteUser_whenUserExists() throws Exception {
        doNothing().when(userService).delete(1L);

        mockMvc.perform(delete(END_POINT + "/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenUserNonExists_thanReturn404() throws Exception {
        doThrow(new ResourceNotFoundException(String.format(USER_NOT_FOUND_MSG, 1)))
                .when(userService).delete(1L);

        mockMvc.perform(delete(END_POINT + "/{id}", 1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }
}