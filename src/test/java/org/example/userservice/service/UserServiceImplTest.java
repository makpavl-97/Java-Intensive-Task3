package org.example.userservice.service;

import org.example.userservice.dao.UserDao;
import org.example.userservice.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        // Дополнительной инициализации не требуется,
        // аннотации @Mock и @InjectMocks обрабатываются MockitoExtension.
    }

    @Test
    void createUser_shouldCreateAndPersistUser() {
        String name = "Alice";
        String email = "alice@example.com";
        int age = 25;

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        User created = userService.createUser(name, email, age);

        // Проверка вызова DAO
        verify(userDao, times(1)).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertEquals(name, saved.getName());
        assertEquals(email, saved.getEmail());
        assertEquals(age, saved.getAge());

        // Метод createUser возвращает ту же сущность
        assertEquals(name, created.getName());
        assertEquals(email, created.getEmail());
        assertEquals(age, created.getAge());
    }

    @Test
    void getUserById_shouldReturnUserWhenExists() {
        Long id = 1L;
        User user = new User("Bob", "bob@example.com", 30);
        user.setId(id);

        when(userDao.findById(id)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(id);

        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        verify(userDao, times(1)).findById(id);
    }

    @Test
    void getUserById_shouldReturnEmptyWhenNotFound() {
        Long id = 2L;
        when(userDao.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(id);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findById(id);
    }

    @Test
    void getAllUsers_shouldReturnListFromDao() {
        List<User> users = Arrays.asList(
                new User("A", "a@example.com", 20),
                new User("B", "b@example.com", 21)
        );
        when(userDao.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        assertSame(users, result);
        verify(userDao, times(1)).findAll();
    }

    @Test
    void updateUser_shouldUpdateExistingUser() {
        Long id = 1L;
        User existing = new User("Old", "old@example.com", 40);
        existing.setId(id);

        when(userDao.findById(id)).thenReturn(Optional.of(existing));

        Optional<User> result = userService.updateUser(id, "New", "new@example.com", 41);

        assertTrue(result.isPresent());
        User updated = result.get();
        assertEquals("New", updated.getName());
        assertEquals("new@example.com", updated.getEmail());
        assertEquals(41, updated.getAge());

        verify(userDao, times(1)).findById(id);
        verify(userDao, times(1)).update(existing);
    }

    @Test
    void updateUser_shouldReturnEmptyWhenUserNotFound() {
        Long id = 1L;
        when(userDao.findById(id)).thenReturn(Optional.empty());

        Optional<User> result = userService.updateUser(id, "New", "new@example.com", 41);

        assertTrue(result.isEmpty());
        verify(userDao, times(1)).findById(id);
        verify(userDao, never()).update(any());
    }

    @Test
    void deleteUser_shouldDeleteWhenUserExists() {
        Long id = 1L;
        User existing = new User("Name", "name@example.com", 20);
        existing.setId(id);

        when(userDao.findById(id)).thenReturn(Optional.of(existing));

        boolean deleted = userService.deleteUser(id);

        assertTrue(deleted);
        verify(userDao, times(1)).findById(id);
        verify(userDao, times(1)).deleteById(id);
    }

    @Test
    void deleteUser_shouldReturnFalseWhenUserNotFound() {
        Long id = 1L;
        when(userDao.findById(id)).thenReturn(Optional.empty());

        boolean deleted = userService.deleteUser(id);

        assertFalse(deleted);
        verify(userDao, times(1)).findById(id);
        verify(userDao, never()).deleteById(anyLong());
    }
}
