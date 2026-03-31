package se.mo.xarbetemonolitisk.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.mo.xarbetemonolitisk.dto.user.CreateUserRequest;
import se.mo.xarbetemonolitisk.dto.user.UserResponse;
import se.mo.xarbetemonolitisk.entity.User;
import se.mo.xarbetemonolitisk.exception.ConflictException;
import se.mo.xarbetemonolitisk.exception.ResourceNotFoundException;
import se.mo.xarbetemonolitisk.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userRepository.findByEmail(request.getEmail()).ifPresent(existing -> {
            throw new ConflictException("User with email already exists: " + request.getEmail());
        });

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());

        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        return response;
    }
}
