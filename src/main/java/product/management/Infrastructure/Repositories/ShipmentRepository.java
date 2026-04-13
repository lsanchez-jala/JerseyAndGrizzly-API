package product.management.Infrastructure.Repositories;


import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Domain.DTO.Shipment.ShipmentRequest;
import product.management.Domain.Models.Shipment;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class ShipmentRepository {

    private final DataSource dataSource;

    @Inject
    public ShipmentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
//        ensureTable();
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS shipment (
                    id              UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
                    order_id        UUID                        NOT NULL REFERENCES orders(id) ON DELETE CASCADE UNIQUE,
                    tracking_code   VARCHAR(255)                NOT NULL UNIQUE,
                    carrier         VARCHAR(255)                NOT NULL,
                    status          VARCHAR(100)                NOT NULL,
                    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
                );
                """;

        try (Connection connection = dataSource.getConnection()) {
            Statement st = connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure shipment table", e);
        }
    }

    private Shipment mapRow(ResultSet rs) throws SQLException {
        Shipment shipment = new Shipment();
        shipment.setId(rs.getObject("id", UUID.class));
        shipment.setOrderId(rs.getObject("order_id", UUID.class));
        shipment.setTrackingCode(rs.getString("tracking_code"));
        shipment.setCarrier(rs.getString("carrier"));
        shipment.setStatus(rs.getString("status"));
        shipment.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        shipment.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return shipment;
    }

    public List<Shipment> findAll() {
        String sql = "SELECT * FROM shipment";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Shipment> shipments = new ArrayList<>();
            while (rs.next()) {
                shipments.add(mapRow(rs));
            }
            return shipments;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve shipments", e);
        }
    }

    public Shipment findById(UUID id) {
        String sql = "SELECT * FROM shipment WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve shipment by id", e);
        }
        return null;
    }

    public Shipment findByOrderId(UUID orderId) {
        String sql = "SELECT * FROM shipment WHERE order_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve shipment by order id", e);
        }
        return null;
    }

    public Shipment findByTrackingCode(String trackingCode) {
        String sql = "SELECT * FROM shipment WHERE tracking_code = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, trackingCode);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve shipment by tracking code", e);
        }
        return null;
    }

    public Shipment save(ShipmentRequest request) {
        String sql = """
                INSERT INTO shipment (order_id, tracking_code, carrier, status)
                VALUES (?, ?, ?, ?)
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, request.orderId());
            ps.setString(2, request.trackingCode());
            ps.setString(3, request.carrier());
            ps.setString(4, request.status());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert shipment", e);
        }
        return null;
    }

    public Shipment save(UUID id, ShipmentRequest request) {
        String sql = """
                UPDATE shipment
                SET order_id      = ?,
                    tracking_code = ?,
                    carrier       = ?,
                    status        = ?,
                    updated_at    = now()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, request.orderId());
            ps.setString(2, request.trackingCode());
            ps.setString(3, request.carrier());
            ps.setString(4, request.status());
            ps.setObject(5, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No shipment found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update shipment", e);
        }
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM shipment WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("No shipment found with id: " + id);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete shipment", e);
        }
    }
}
