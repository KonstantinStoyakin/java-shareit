package ru.practicum.shareit.user;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    private final Map<Long, User> users = new HashMap<>();
    private Long idCounter = 1L;

    @Override
    public User createUser(User user) {
        validateUserEmail(user.getEmail());
        user.setId(idCounter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User updateUser(Long userId, User user) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("User not found");
        }

        User existingUser = users.get(userId);

        if (user.getName() != null) {
            existingUser.setName(user.getName());
        }

        if (user.getEmail() != null) {
            if (!user.getEmail().equals(existingUser.getEmail())) {
                validateUserEmail(user.getEmail());
            }
            existingUser.setEmail(user.getEmail());
        }

        return existingUser;
    }

    @Override
    public User getUser(Long userId) {
        if (!users.containsKey(userId)) {
            throw new NotFoundException("User not found");
        }
        return users.get(userId);
    }

    @Override
    public void deleteUser(Long userId) {
        users.remove(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    private void validateUserEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email must not be null");
        }

        users.values().stream()
                .filter(u -> email.equals(u.getEmail())) // безопасно: email точно не null
                .findFirst()
                .ifPresent(u -> {
                    throw new ConflictException("Email already exists");
                });
    }
}
