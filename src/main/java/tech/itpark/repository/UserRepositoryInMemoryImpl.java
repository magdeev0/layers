package tech.itpark.repository;

import tech.itpark.entity.UserEntity;

import java.util.*;

public class UserRepositoryInMemoryImpl implements UserRepository {
  private long nextId = 1;
  private final Map<Long, UserEntity> idToEntity = new HashMap<>();
  private final Map<String, UserEntity> loginToEntity = new HashMap<>(); // намеренная избыточность

  @Override
  public List<UserEntity> findAll() {
    return List.copyOf(idToEntity.values());
  }

  @Override
  public Optional<UserEntity> findById(Long id) {
    // Optional.of(null) -> NPE
    return Optional.ofNullable(idToEntity.get(id));
  }

  @Override
  public UserEntity save(UserEntity entity) {
    // id = 0 ? новый : старый (обновляем)
    // FIXME: login-то должен быть уникальным
    if (entity.getId() == 0) {
      entity.setId(nextId++);
    }
    // FIXME: check if user didn't change login
    loginToEntity.put(entity.getLogin(), entity);
    return idToEntity.put(entity.getId(), entity);
  }

  @Override
  public boolean removeById(Long id) {
    idToEntity.get(id).setRemoved(true);
    return true;
  }

  @Override
  public boolean existsByLogin(String login) {
    return loginToEntity.containsKey(login);
  }

  @Override
  public Optional<UserEntity> findByLogin(String login) {
    return Optional.ofNullable(loginToEntity.get(login));
  }
}
