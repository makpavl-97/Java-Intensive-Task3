package org.example.userservice.service;

import org.example.userservice.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {

    User createUser(String name, String email, int age);

    Optional<User> getUserById(Long id);

    List<User> getAllUsers();

    Optional<User> updateUser(Long id, String newName, String newEmail, Integer newAge);

    boolean deleteUser(Long id);
}
