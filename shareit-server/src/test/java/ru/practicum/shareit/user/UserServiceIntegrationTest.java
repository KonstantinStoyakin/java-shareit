package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
@Import(UserServiceImpl.class)
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private TestEntityManager em;

    @Test
    void createAndGetUser_shouldWork() {
        User user = new User(null, "Test", "test@mail.ru");
        User created = userService.createUser(user);

        User found = userService.getUser(created.getId());

        assertEquals(created.getId(), found.getId());
    }
}
