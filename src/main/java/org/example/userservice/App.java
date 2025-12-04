package org.example.userservice;

import org.example.userservice.dao.UserDaoImpl;
import org.example.userservice.entity.User;
import org.example.userservice.service.UserService;
import org.example.userservice.service.UserServiceImpl;
import org.example.userservice.util.HibernateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class App {

    private static final Logger log = LoggerFactory.getLogger(App.class);
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        log.info("Запуск консольного приложения user-service");

        UserService userService = new UserServiceImpl(new UserDaoImpl());

        boolean running = true;
        while (running) {
            printMenu();
            int choice = readInt("Выберите пункт меню: ");

            try {
                switch (choice) {
                    case 1 -> createUser(userService);
                    case 2 -> findUserById(userService);
                    case 3 -> listAllUsers(userService);
                    case 4 -> updateUser(userService);
                    case 5 -> deleteUser(userService);
                    case 0 -> {
                        running = false;
                        log.info("Завершение работы приложения");
                    }
                    default -> System.out.println("Неизвестный пункт меню");
                }
            } catch (Exception ex) {
                System.out.println("Произошла ошибка: " + ex.getMessage());
                log.error("Ошибка при выполнении операции", ex);
            }
        }

        HibernateUtil.shutdown();
        scanner.close();
    }

    private static void printMenu() {
        System.out.println();
        System.out.println("==== USER SERVICE ====");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по id");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
    }

    private static void createUser(UserService userService) {
        System.out.println("--- Создание пользователя ---");
        System.out.print("Имя: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        int age = readInt("Возраст: ");

        User user = userService.createUser(name, email, age);
        System.out.println("Пользователь создан с id: " + user.getId());
    }

    private static void findUserById(UserService userService) {
        System.out.println("--- Поиск пользователя по id ---");
        long id = readLong("id пользователя: ");

        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isPresent()) {
            System.out.println("Найден пользователь: " + userOpt.get());
        } else {
            System.out.println("Пользователь с id " + id + " не найден");
        }
    }

    private static void listAllUsers(UserService userService) {
        System.out.println("--- Список всех пользователей ---");
        List<User> users = userService.getAllUsers();
        if (users.isEmpty()) {
            System.out.println("Пользователи отсутствуют");
        } else {
            users.forEach(System.out::println);
        }
    }

    private static void updateUser(UserService userService) {
        System.out.println("--- Обновление пользователя ---");
        long id = readLong("id пользователя для обновления: ");

        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            System.out.println("Пользователь с id " + id + " не найден");
            return;
        }

        User user = userOpt.get();
        System.out.println("Текущие данные: " + user);

        System.out.print("Новое имя (пусто, чтобы не менять): ");
        String newName = scanner.nextLine();
        if (newName.isBlank()) {
            newName = null;
        }

        System.out.print("Новый email (пусто, чтобы не менять): ");
        String newEmail = scanner.nextLine();
        if (newEmail.isBlank()) {
            newEmail = null;
        }

        System.out.print("Новый возраст (пусто, чтобы не менять): ");
        String ageInput = scanner.nextLine();
        Integer newAge = null;
        if (!ageInput.isBlank()) {
            try {
                newAge = Integer.parseInt(ageInput.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Возраст не изменен: некорректное число");
            }
        }

        Optional<User> updatedOpt = userService.updateUser(id, newName, newEmail, newAge);
        if (updatedOpt.isPresent()) {
            System.out.println("Пользователь обновлен: " + updatedOpt.get());
        } else {
            System.out.println("Обновление не выполнено");
        }
    }

    private static void deleteUser(UserService userService) {
        System.out.println("--- Удаление пользователя ---");
        long id = readLong("id пользователя для удаления: ");

        boolean deleted = userService.deleteUser(id);
        if (deleted) {
            System.out.println("Пользователь удален");
        } else {
            System.out.println("Пользователь с таким id не найден");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Integer.parseInt(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Введите целое число");
            }
        }
    }

    private static long readLong(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            try {
                return Long.parseLong(line.trim());
            } catch (NumberFormatException ex) {
                System.out.println("Введите целое число");
            }
        }
    }
}
