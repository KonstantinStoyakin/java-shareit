package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.exception.ConflictException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_whenEmailExists_shouldThrowConflictException() {
        User user = new User(null, "Name", "existing@example.com");
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(user));
        verify(userRepository).existsByEmail("existing@example.com");
    }

    @Test
    void updateUser_whenEmailExists_shouldThrowConflictException() {
        User existingUser = new User(1L, "Name", "old@example.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        User updates = new User();
        updates.setEmail("existing@example.com");

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, updates));
        verify(userRepository).existsByEmail("existing@example.com");
    }

    @Test
    void updateUser_whenNoUpdates_shouldReturnOriginalUser() {
        User existingUser = new User(1L, "Name", "email@example.com");
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User updates = new User();
        User result = userService.updateUser(1L, updates);

        assertEquals(existingUser, result);
        verify(userRepository, never()).existsByEmail(anyString());
    }
}