package tech.itpark.repository;

import tech.itpark.entity.UserEntity;
import tech.itpark.exception.DataAccessException;
import tech.itpark.jdbc.RowMapper;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UserRepositoryJDBCImpl implements UserRepository {
    private final Connection connection;
    private final RowMapper<UserEntity> mapper = rs -> {
        try {
            return new UserEntity(
                    rs.getLong("id"),
                    rs.getString("login"),
                    rs.getString("password"),
                    rs.getString("name"),
                    rs.getString("secret"),
                    Set.of((String[]) rs.getArray("roles").getArray()),
                    rs.getBoolean("removed"),
                    rs.getLong("created")
            );
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    };

    public UserRepositoryJDBCImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<UserEntity> findAll() {
        final var sql = "SELECT id, login, password, name, secret, roles, EXTRACT(EPOCH FROM created) created, removed FROM users ORDER BY id";
        try (
                final Statement stmt = connection.createStatement();
                final ResultSet rs = stmt.executeQuery(sql)
        ) {
            List<UserEntity> result = new LinkedList<>();
            while (rs.next()) {
                final UserEntity entity = mapper.map(rs);
                result.add(entity);
            }
            return result;
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Optional<UserEntity> findById(Long id) {
        final var sql = "SELECT id, login, password, name, secret, roles, EXTRACT(EPOCH FROM created) created, removed FROM users WHERE id = ?";
        try (
                final var stmt = connection.prepareStatement(sql)
        ) {
            stmt.setLong(1, id);
            try (final var rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public UserEntity save(UserEntity entity) {
        if (entity.getId() == 0) {
            String sql = "INSERT INTO users (login, password, name, secret, roles, removed, created) values (?, ?, ?, ?, ?, ?, ?)";
            try (final var stmt = connection.prepareStatement(sql)) {
                stmt.setString(1, entity.getLogin());
                stmt.setString(2, entity.getPassword());
                stmt.setString(3, entity.getName());
                stmt.setString(4, entity.getSecret());
                stmt.setArray(5, connection.createArrayOf("TEXT", entity.getRoles().toArray()));
                stmt.setBoolean(6, entity.isRemoved());
                stmt.setLong(7, entity.getCreated());
                stmt.execute();
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }
        String sql = "UPDATE users SET login = ?, password = ?, name = ?, secret = ?, roles = ? WHERE id = ?";
        try (final var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, entity.getLogin());
            stmt.setString(2, entity.getPassword());
            stmt.setString(3, entity.getName());
            stmt.setString(4, entity.getSecret());
            stmt.setArray(5, connection.createArrayOf("TEXT", entity.getRoles().toArray()));
            stmt.setLong(6, entity.getId());
            stmt.execute();
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
        return entity;
    }

    @Override
    public boolean removeById(Long id) {
        if (findById(id).isPresent()) {
            String sql = "UPDATE users SET removed = true WHERE id = ?";
            try (final var stmt = connection.prepareStatement(sql)) {
                stmt.setLong(1, id);
                stmt.execute();
                return true;
            } catch (SQLException e) {
                throw new DataAccessException(e);
            }
        }

        return false;
    }

    @Override
    public boolean existsByLogin(String login) {
        final var sql = "SELECT login FROM users WHERE login = ?";
        try (
                final var stmt = connection.prepareStatement(sql)
        ) {
            stmt.setString(1, login);
            try (final var rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Optional<UserEntity> findByLogin(String login) {
        final var sql = "SELECT id, login, password, name, secret, roles, EXTRACT(EPOCH FROM created) created, removed FROM users WHERE login = ?";
        try (
                final var stmt = connection.prepareStatement(sql)
        ) {
            stmt.setString(1, login);
            try (final var rs = stmt.executeQuery()) {
                return rs.next() ? Optional.of(mapper.map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DataAccessException(e);
        }
    }
}