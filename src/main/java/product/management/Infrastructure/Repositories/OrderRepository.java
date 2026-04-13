package product.management.Infrastructure.Repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Domain.DTO.Order.OrderRequest;
import product.management.Domain.Enums.OrderStatus;
import product.management.Domain.Models.Order;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class OrderRepository {

    private final DataSource dataSource;

    @Inject
    public OrderRepository(DataSource dataSource) {
        this.dataSource = dataSource;
//        ensureTable();
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS orders (
                    id              UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
                    customer_id     UUID                        NULL REFERENCES customer(id),
                    status          VARCHAR(50)                 NOT NULL,
                    total_amount    NUMERIC(12, 2)              NOT NULL CHECK (total_amount >= 0),
                    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
                );
                """;

        try (Connection connection = dataSource.getConnection()) {
            Statement st = connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure orders table", e);
        }
    }

    private Order mapRow(ResultSet rs) throws SQLException {
        Order order = new Order();
        order.setId(rs.getObject("id", UUID.class));
        order.setCustomerId(rs.getObject("customer_id", UUID.class));
        order.setStatus(OrderStatus.valueOf(rs.getString("status")));
        order.setTotalAmount(rs.getFloat("total_amount"));
        order.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        order.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return order;
    }

    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Order> orders = new ArrayList<>();
            while (rs.next()) {
                orders.add(mapRow(rs));
            }
            return orders;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve orders", e);
        }
    }

    public Order findById(UUID id) {
        String sql = "SELECT * FROM orders WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve order by id", e);
        }
        return null;
    }

    public List<Order> findByCustomerId(UUID customerId) {
        String sql = "SELECT * FROM orders WHERE customer_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, customerId);

            try (ResultSet rs = ps.executeQuery()) {
                List<Order> orders = new ArrayList<>();
                while (rs.next()) {
                    orders.add(mapRow(rs));
                }
                return orders;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve orders for customer", e);
        }
    }

    public Order save(Order request) {
        String sql = """
                INSERT INTO orders (customer_id, status, total_amount, created_at, updated_at)
                VALUES (?, ?, ?, now(), now())
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, request.getCustomerId());
            ps.setString(2, request.getStatus().name());
            ps.setFloat(3, request.getTotalAmount());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert order", e);
        }
        return null;
    }

    public Order save(UUID id, Order request) {
        String sql = """
                UPDATE orders
                SET customer_id  = ?,
                    total_amount = ?,
                    updated_at   = now()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, request.getCustomerId());
            ps.setFloat(2, request.getTotalAmount());
            ps.setObject(3, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No order found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM orders WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("No order found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete order", e);
        }
    }

    public Order updateStatus(UUID orderId, OrderStatus newStatus){
        String sql = """
                UPDATE orders
                SET status       = ?,
                    updated_at   = now()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setObject(2, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No order found with id: " + orderId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order", e);
        }
    }
}
