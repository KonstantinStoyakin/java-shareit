package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.practicum.shareit.TestConfig;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({TestConfig.class, UserServiceImpl.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TestEntityManager em;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");

        user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
    }

    @Test
    void createUser_shouldSaveUserToDatabase() {
        User createdUser = userService.createUser(user1);

        assertNotNull(createdUser.getId());
        assertEquals(user1.getName(), createdUser.getName());
        assertEquals(user1.getEmail(), createdUser.getEmail());

        User fromDb = em.find(User.class, createdUser.getId());
        assertEquals(createdUser, fromDb);
    }

    @Test
    void updateUser_shouldUpdateUserFields() {
        User savedUser = userRepository.save(user1);

        User updates = new User();
        updates.setName("Updated Name");

        User updatedUser = userService.updateUser(savedUser.getId(), updates);

        assertEquals(savedUser.getId(), updatedUser.getId());
        assertEquals("Updated Name", updatedUser.getName());
        assertEquals(savedUser.getEmail(), updatedUser.getEmail());
    }

    @Test
    void updateUser_shouldUpdateEmailWhenUnique() {
        User savedUser = userRepository.save(user1);
        userRepository.save(user2);

        User updates = new User();
        updates.setEmail("user2-updated@example.com");

        User updatedUser = userService.updateUser(savedUser.getId(), updates);

        assertEquals("user2-updated@example.com", updatedUser.getEmail());
    }

    @Test
    void getUser_shouldReturnUserFromDatabase() {
        User savedUser = userRepository.save(user1);

        User foundUser = userService.getUser(savedUser.getId());

        assertEquals(savedUser, foundUser);
    }

    @Test
    void getAllUsers_shouldReturnAllUsersFromDatabase() {
        userRepository.save(user1);
        userRepository.save(user2);

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    void deleteUser_shouldRemoveUserFromDatabase() {
        User savedUser = userRepository.save(user1);

        userService.deleteUser(savedUser.getId());

        assertFalse(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void createUser_shouldThrowWhenEmailExists() {
        userRepository.save(user1);

        User duplicateEmailUser = new User();
        duplicateEmailUser.setName("Duplicate");
        duplicateEmailUser.setEmail(user1.getEmail());

        assertThrows(ConflictException.class, () -> userService.createUser(duplicateEmailUser));
    }

    @Test
    void updateUser_shouldThrowWhenEmailExists() {
        userRepository.save(user1);
        User savedUser2 = userRepository.save(user2);

        User updates = new User();
        updates.setEmail(user1.getEmail());

        assertThrows(ConflictException.class,
                () -> userService.updateUser(savedUser2.getId(), updates));
    }

    @Test
    void getUser_shouldThrowWhenUserNotFound() {
        assertThrows(NotFoundException.class, () -> userService.getUser(999L));
    }
}
