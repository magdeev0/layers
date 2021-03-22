package tech.itpark;

import tech.itpark.entity.UserEntity;
import tech.itpark.repository.UserRepository;
import tech.itpark.repository.UserRepositoryInMemoryImpl;
import tech.itpark.repository.UserRepositoryJDBCImpl;
import tech.itpark.service.UserService;
import tech.itpark.service.UserServiceDefaultImpl;

import java.sql.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class Main {
  public static void main(String[] args) {
    // жёсткая завязка на конкретный класс
    // я никак не могу повлиять на ЖЦ этого объекта
    // Singleton.getInstance();

    // JDBC connection URL
    // TODO: get from environment
    String dsn = "jdbc:postgresql://localhost:5400/appdb?user=app&password=pass";
    try (Connection connection = DriverManager.getConnection(dsn)) {
      UserRepository repository = new UserRepositoryJDBCImpl(connection);
      UserService service = new UserServiceDefaultImpl(repository);
      List<UserEntity> users = repository.findAll();

      // Just for test
      Statement statement = connection.createStatement();
      // 3 группы методов:
      // Exec -> общего назначения
      // Query -> Select
      // Update -> Insert, Update, Delete

      // * - anti-patter
      ResultSet resultSet = statement.executeQuery("SELECT * FROM users");
      // Iterator -> hasNext() + next()

      // ResultSet -> на позицию до данных
      while (resultSet.next()) { // next -> переходит на следующую строку и возвращает true, если там есть данные
        // // index - starts from 1
        System.out.println(resultSet.getString("created"));
      }
      for (UserEntity user : users) {
        System.out.println(user.isRemoved());
      }
      System.out.println(repository.findById(1L).get().getLogin());
    } catch (SQLException e) {
      e.printStackTrace();
    }
    UserRepository repo = new UserRepositoryInMemoryImpl();
    repo.save(new UserEntity(1, "login", "pass", "secret", "name", new HashSet<>(Arrays.asList("a", "b")), false, 4444));
    System.out.println(Optional.ofNullable(repo.findById(2L).get().getName()));
  }
}
