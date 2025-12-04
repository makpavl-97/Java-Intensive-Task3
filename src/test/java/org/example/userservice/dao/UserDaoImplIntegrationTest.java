package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true) // ВАЖНО: если Docker недоступен, класс тестов будет SKIPPED
class UserDaoImplIntegrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    private UserDao userDao;

    @BeforeAll
    static void beforeAll() {
        // К этому моменту, если Docker есть, контейнер уже стартован Testcontainers Extension-ом.
        // Если Docker нет, весь класс будет пропущен, и сюда управление не зайдет.
        System.setProperty("hibernate.connection.url", POSTGRES.getJdbcUrl());
        System.setProperty("hibernate.connection.username", POSTGRES.getUsername());
        System.setProperty("hibernate.connection.password", POSTGRES.getPassword());

        HibernateUtil.rebuildSessionFactory();
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
        // НЕ вызываем POSTGRES.stop() – это сделает Testcontainers Extension
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();

        SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
        var session = sessionFactory.openSession();
        var tx = session.beginTransaction();
        session.createMutationQuery("delete from User").executeUpdate();
        tx.commit();
        session.close();
    }

    @Test
    void saveAndFindById_shouldPersistAndLoadUser() {
        User user = new User("Integration", "int@example.com", 35);
        userDao.save(user);

        assertNotNull(user.getId());

        Optional<User> loaded = userDao.findById(user.getId());
        assertTrue(loaded.isPresent());
        assertEquals("Integration", loaded.get().getName());
        assertEquals("int@example.com", loaded.get().getEmail());
        assertEquals(35, loaded.get().getAge());
    }

    @Test
    void findAll_shouldReturnAllUsers() {
        userDao.save(new User("User1", "u1@example.com", 20));
        userDao.save(new User("User2", "u2@example.com", 21));

        List<User> users = userDao.findAll();
        assertEquals(2, users.size());
    }

    @Test
    void update_shouldModifyExistingUser() {
        User user = new User("Old", "old@example.com", 40);
        userDao.save(user);

        Long id = user.getId();
        user.setName("New");
        user.setEmail("new@example.com");
        user.setAge(41);

        userDao.update(user);

        Optional<User> reloaded = userDao.findById(id);
        assertTrue(reloaded.isPresent());
        assertEquals("New", reloaded.get().getName());
        assertEquals("new@example.com", reloaded.get().getEmail());
        assertEquals(41, reloaded.get().getAge());
    }

    @Test
    void deleteById_shouldRemoveUser() {
        User user = new User("ToDelete", "del@example.com", 50);
        userDao.save(user);

        Long id = user.getId();
        userDao.deleteById(id);

        Optional<User> loaded = userDao.findById(id);
        assertTrue(loaded.isEmpty());
    }
}
