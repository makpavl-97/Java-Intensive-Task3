package org.example.userservice.dao;

import org.example.userservice.entity.User;
import org.example.userservice.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

public class UserDaoImpl implements UserDao {

    private static final Logger log = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void save(User user) {
        log.info("Начато сохранение пользователя: name={}, email={}, age={}",
                user.getName(), user.getEmail(), user.getAge());

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            session.persist(user);

            tx.commit();
            log.info("Пользователь успешно сохранен: id={}", user.getId());
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
                log.info("Выполнен откат транзакции при сохранении пользователя");
            }
            log.error("Ошибка при сохранении пользователя", ex);
            throw ex;
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        log.info("Поиск пользователя по id={}", id);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, id);

            tx.commit();

            if (user != null) {
                log.info("Пользователь найден: id={}, email={}", user.getId(), user.getEmail());
            } else {
                log.info("Пользователь с id={} не найден", id);
            }

            return Optional.ofNullable(user);
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
                log.info("Выполнен откат транзакции при поиске пользователя по id={}", id);
            }
            log.error("Ошибка при поиске пользователя по id {}", id, ex);
            throw ex;
        }
    }

    @Override
    public List<User> findAll() {
        log.info("Запрос на получение всех пользователей");

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            Query<User> query = session.createQuery("from User", User.class);
            List<User> result = query.getResultList();

            tx.commit();

            log.info("Получен список пользователей: количество={}", result.size());
            return result;
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
                log.info("Выполнен откат транзакции при получении списка пользователей");
            }
            log.error("Ошибка при получении списка пользователей", ex);
            throw ex;
        }
    }

    @Override
    public void update(User user) {
        log.info("Обновление пользователя: id={}, name={}, email={}, age={}",
                user.getId(), user.getName(), user.getEmail(), user.getAge());

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            session.merge(user);

            tx.commit();
            log.info("Пользователь успешно обновлен: id={}", user.getId());
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
                log.info("Выполнен откат транзакции при обновлении пользователя id={}", user.getId());
            }
            log.error("Ошибка при обновлении пользователя", ex);
            throw ex;
        }
    }

    @Override
    public void deleteById(Long id) {
        log.info("Удаление пользователя по id={}", id);

        Transaction tx = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            tx = session.beginTransaction();

            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                log.info("Пользователь найден и удален: id={}", id);
            } else {
                log.info("Пользователь с id={} не найден, удаление не выполнено", id);
            }

            tx.commit();
            log.info("Транзакция удаления пользователя id={} успешно завершена", id);
        } catch (Exception ex) {
            if (tx != null) {
                tx.rollback();
                log.info("Выполнен откат транзакции при удалении пользователя id={}", id);
            }
            log.error("Ошибка при удалении пользователя с id " + id, ex);
            throw ex;
        }
    }
}
