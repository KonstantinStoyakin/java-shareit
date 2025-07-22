package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceIntegrationTest {

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
    void createAndGetUser_shouldWork() {
        User user = new User(null, "Test", "test@mail.ru");
        User created = userService.createUser(user);

        User found = userService.getUser(created.getId());

        assertEquals(created.getId(), found.getId());
    }

    @Test
    void updateUser_shouldNotUpdateEmailWhenSame() {
        User savedUser = userRepository.save(user1);

        User updates = new User();
        updates.setEmail(user1.getEmail());

        User updatedUser = userService.updateUser(savedUser.getId(), updates);

        assertEquals(user1.getEmail(), updatedUser.getEmail());
    }

    @Test
    void deleteUser_shouldNotThrowWhenUserNotExist() {
        assertDoesNotThrow(() -> userService.deleteUser(999L));
    }
}
