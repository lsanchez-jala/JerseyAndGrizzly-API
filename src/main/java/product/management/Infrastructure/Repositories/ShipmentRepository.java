package product.management.Infrastructure.Repositories;


import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Domain.Enums.ShipmentStatus;
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
    }


    private Shipment mapRow(ResultSet rs) throws SQLException {
        Shipment shipment = new Shipment();
        shipment.setId(rs.getObject("id", UUID.class));
        shipment.setTrackingCode(rs.getString("tracking_code"));
        shipment.setCarrier(rs.getString("carrier"));
        shipment.setStatus(ShipmentStatus.valueOf(rs.getString("status")));
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

    public Shipment save(Shipment request) {
        String sql = """
                INSERT INTO shipment (tracking_code, carrier, status, created_at, updated_at)
                VALUES ( ?, ?, ?, now(), now())
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.getTrackingCode());
            ps.setString(2, request.getCarrier());
            ps.setString(3, request.getStatus().name());

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

    public Shipment save(UUID id, Shipment request) {
        String sql = """
                UPDATE shipment
                SET carrier       = ?,
                    updated_at    = now()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.getCarrier());
            ps.setObject(2, id);

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

    public Shipment updateStatus(UUID shipmentId, ShipmentStatus newStatus){
        String sql = """
                UPDATE shipment
                SET status       = ?,
                    updated_at   = now()
                WHERE id = ?
                RETURNING *
                """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, newStatus.name());
            ps.setObject(2, shipmentId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No shipment found with id: " + shipmentId);
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
