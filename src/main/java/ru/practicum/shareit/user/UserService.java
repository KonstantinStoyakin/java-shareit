package ru.practicum.shareit.user;

import java.util.List;

public interface UserService {
    User createUser(User user);
    User updateUser(Long userId, User user);
    User getUser(Long userId);
    void deleteUser(Long userId);
    List<User> getAllUsers();
}
