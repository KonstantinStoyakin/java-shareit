package ru.practicum.shareit.user;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public User createUser(User user) {
        validateUserEmail(user.getEmail());
        return userRepository.save(user);
    }

    @Override
    public User updateUser(Long userId, User userUpdates) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (userUpdates.getName() != null && !userUpdates.getName().isBlank()) {
            existingUser.setName(userUpdates.getName());
        }

        if (userUpdates.getEmail() != null && !userUpdates.getEmail().isBlank()) {
            if (!userUpdates.getEmail().equals(existingUser.getEmail())) {
                validateUserEmail(userUpdates.getEmail());
                existingUser.setEmail(userUpdates.getEmail());
            }
        }

        return userRepository.save(existingUser);
    }

    @Override
    public User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Override
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private void validateUserEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
    }
}