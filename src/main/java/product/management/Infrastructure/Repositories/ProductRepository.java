package product.management.Infrastructure.Repositories;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import product.management.Domain.DTO.Product.ProductRequest;
import product.management.Domain.Models.Product;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Singleton
public class ProductRepository {

    private final DataSource dataSource;

    @Inject
    public ProductRepository(DataSource dataSource) {
        this.dataSource = dataSource;
//        ensureTable();
    }

    private void ensureTable() {
        String sql = """
                CREATE TABLE IF NOT EXISTS product (
                    id            UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                    name          VARCHAR(255)    NOT NULL,
                    sku           VARCHAR(100)    NOT NULL UNIQUE,
                    price         NUMERIC(12, 2)  NOT NULL CHECK (price >= 0),
                    stock         INT             NOT NULL DEFAULT 0 CHECK (stock >= 0),
                    category      VARCHAR(100)
                );
                """;

        try (Connection connection = dataSource.getConnection()) {
            Statement st = connection.createStatement();
            st.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure users table", e);
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getObject("id", UUID.class));
        product.setName(rs.getString("name"));
        product.setSku(rs.getString("sku"));
        product.setPrice(rs.getFloat("price"));
        product.setStock(rs.getInt("stock"));
        product.setCategory(rs.getString("category"));
        return product;
    }

    public List<Product> findAll() {
        String sql = """
                SELECT * FROM product
                """;
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Product> products = new ArrayList<>();
            while (rs.next()) {
                products.add(mapRow(rs));
            }
            return products;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve all products", e);
        }
    }

    public Product findById(UUID id) {
        String sql = """
                SELECT * FROM product WHERE id = ?
                """;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setObject(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve a product", e);
        }
        return null;
    }

    public Product findBySku(String sku) {
        String sql = """
                SELECT * FROM product WHERE sku = ?
                """;
        try (Connection connection = dataSource.getConnection()) {
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, sku);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve a product", e);
        }
        return null;
    }

    public Product save(ProductRequest request){
        String sql = """
                INSERT INTO product (name, sku, price, stock, category)\s
                VALUES (?,?,?,?,?) RETURNING *
               \s""";

        try(Connection connection =  dataSource.getConnection()){
            PreparedStatement ps = connection.prepareStatement(sql);

            ps.setString(1, request.name());
            ps.setString(2, request.sku());
            ps.setFloat(3, request.price());
            ps.setInt(4, request.stock());
            ps.setString(5, request.category());

            try (ResultSet rs =ps.executeQuery()){
                if(rs.next()){
                    return mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert the product", e);
        }
        return null;
    }

    public void delete(UUID id) {
        String sql = "DELETE FROM product WHERE id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setObject(1, id);
            int affectedRows = ps.executeUpdate();

            if (affectedRows == 0) {
                throw new RuntimeException("No product found with id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete the product", e);
        }
    }

    public Product save(UUID id, Product request) {
        String sql = """
            UPDATE product
            SET name = ?, sku = ?, price = ?, stock = ?, category = ?
            WHERE id = ?
            RETURNING *
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, request.getName());
            ps.setString(2, request.getSku());
            ps.setFloat(3, request.getPrice());
            ps.setInt(4, request.getStock());
            ps.setString(5, request.getCategory());
            ps.setObject(6, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs);
                }
                throw new RuntimeException("No product found with id: " + id);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update the product", e);
        }
    }
}
