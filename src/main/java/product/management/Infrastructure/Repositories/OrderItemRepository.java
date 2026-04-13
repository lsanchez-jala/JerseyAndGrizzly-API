package product.management.Infrastructure.Repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Domain.DTO.OrderItem.OrderItemRequest;
import product.management.Domain.Models.OrderItem;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class OrderItemRepository {

    private final DataSource dataSource;

    @Inject
    public OrderItemRepository(DataSource dataSource) {
        this.dataSource = dataSource;
//        ensureTable();
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS order_item (
                    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                    order_id    UUID            NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                    product_id  UUID            NOT NULL REFERENCES product(id) ON DELETE RESTRICT,
                    quantity    INT             NOT NULL CHECK (quantity > 0),
                    unit_price  NUMERIC(12, 2)  NOT NULL CHECK (unit_price >= 0)
                );
                """;

        try (Connection connection = dataSource.getConnection()) {
            Statement st = connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure order_item table", e);
        }
    }

    private OrderItem mapRow(ResultSet rs) throws SQLException {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(rs.getObject("id", UUID.class));
        orderItem.setOrderId(rs.getObject("order_id", UUID.class));
        orderItem.setProductId(rs.getObject("product_id", UUID.class));
        orderItem.setQuantity(rs.getInt("quantity"));
        orderItem.setUnitPrice(rs.getFloat("unit_price"));
        return orderItem;
    }

    public List<OrderItem> findAll() {
        String sql = "SELECT * FROM order_item";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<OrderItem> items = new ArrayList<>();
            while (rs.next()) {
                items.add(mapRow(rs));
            }
            return items;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve order items", e);
        }
    }

    public OrderItem findById(UUID id) {
        String sql = "SELECT * FROM order_item WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve order item by id", e);
        }
        return null;
    }

    public List<OrderItem> findByOrderId(UUID orderId) {
        String sql = "SELECT * FROM order_item WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
                return items;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve order items by order id", e);
        }
    }

    public List<OrderItem> findByProductId(UUID productId) {
        String sql = "SELECT * FROM order_item WHERE product_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    items.add(mapRow(rs));
                }
                return items;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve order items by product id", e);
        }
    }

    public OrderItem save(OrderItemRequest request) {
        String sql = """
                INSERT INTO order_item (order_id, product_id, quantity, unit_price)
                VALUES (?, ?, ?, ?)
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, request.orderId());
            ps.setObject(2, request.productId());
            ps.setInt(3, request.quantity());
            ps.setFloat(4, request.unitPrice());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert order item", e);
        }
        return null;
    }

    public OrderItem save(UUID id, OrderItemRequest request) {
        String sql = """
                UPDATE order_item
                SET order_id   = ?,
                    product_id = ?,
                    quantity   = ?,
                    unit_price = ?
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, request.orderId());
            ps.setObject(2, request.productId());
            ps.setInt(3, request.quantity());
            ps.setFloat(4, request.unitPrice());
            ps.setObject(5, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No order item found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order item", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM order_item WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("No order item found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete order item", e);
        }
    }

    public void deleteByOrderId(UUID orderId) {
        String sql = "DELETE FROM order_item WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, orderId);
            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete order items for order id: " + orderId, e);
        }
    }
}