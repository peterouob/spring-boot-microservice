package com.peter.user_service.service;

import com.peter.user_service.dto.UserDto;
import com.peter.user_service.entity.User;
import com.peter.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDto createUser(UserDto userDto) {
        log.info("Creating user: {}", userDto);

        final User createdUser = User.builder()
                .name(userDto.getName())
                .surname(userDto.getSurname())
                .email(userDto.getEmail())
                .address(userDto.getAddress())
                .alerting(userDto.isAlerting())
                .energyAlertingThreshold(userDto.getEnergyAlertingThreshold())
                .build();

        User savedUser = userRepository.save(createdUser);
        return toUserDto(savedUser);
    }

    public UserDto getUserById(Long id) {
        log.info("Retrieving user by id: {}", id);
        return userRepository.findById(id).map(this::toUserDto).orElse(null);
    }

    public void updateUser(Long id, UserDto userDto) {
        log.info("Updating user: {}", userDto);
        User user = userRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("User not found with id: " + id));

        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());
        user.setAddress(userDto.getAddress());
        user.setAlerting(userDto.isAlerting());
        user.setEnergyAlertingThreshold(userDto.getEnergyAlertingThreshold());

        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        User user = userRepository.findById(id).orElseThrow(()-> new IllegalArgumentException("User not found with id: " + id));

        userRepository.delete(user);
    }

    private UserDto toUserDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .surname(user.getSurname())
                .email(user.getEmail())
                .address(user.getAddress())
                .alerting(user.isAlerting())
                .energyAlertingThreshold(user.getEnergyAlertingThreshold())
                .build();
    }
}
