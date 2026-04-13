package product.management.Infrastructure.Repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Domain.DTO.Customer.CustomerRequest;
import product.management.Domain.Models.Customer;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class CustomerRepository {

    private final DataSource dataSource;

    @Inject
    public CustomerRepository(DataSource dataSource) {
        this.dataSource = dataSource;
//        ensureTable();
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS customer (
                    id          UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
                    first_name  VARCHAR(255)                NOT NULL,
                    last_name   VARCHAR(255)                NOT NULL,
                    email       VARCHAR(255)                NOT NULL UNIQUE,
                    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                    updated_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
                );
                """;

        try (Connection connection = dataSource.getConnection()) {
            Statement st = connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure customer table", e);
        }
    }

    private Customer mapRow(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setId(rs.getObject("id", UUID.class));
        customer.setFirstName(rs.getString("first_name"));
        customer.setLastName(rs.getString("last_name"));
        customer.setEmail(rs.getString("email"));
        customer.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        customer.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return customer;
    }

    public List<Customer> findAll() {
        String sql = "SELECT * FROM customer";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Customer> customers = new ArrayList<>();
            while (rs.next()) {
                customers.add(mapRow(rs));
            }
            return customers;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve customers", e);
        }
    }

    public Customer findById(UUID id) {
        String sql = "SELECT * FROM customer WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve customer by id", e);
        }
        return null;
    }

    public Customer findByEmail(String email) {
        String sql = "SELECT * FROM customer WHERE email = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, email);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve customer by email", e);
        }
        return null;
    }

    public Customer save(CustomerRequest request) {
        String sql = """
                INSERT INTO customer (first_name, last_name, email)
                VALUES (?, ?, ?)
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.firstName());
            ps.setString(2, request.lastName());
            ps.setString(3, request.email());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert customer", e);
        }
        return null;
    }

    public Customer save(UUID id, CustomerRequest request) {
        String sql = """
                UPDATE customer
                SET first_name = ?,
                    last_name  = ?,
                    email      = ?,
                    updated_at = now()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.firstName());
            ps.setString(2, request.lastName());
            ps.setString(3, request.email());
            ps.setObject(4, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No customer found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update customer", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM customer WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("No customer found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete customer", e);
        }
    }
}