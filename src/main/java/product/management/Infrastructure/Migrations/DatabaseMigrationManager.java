package product.management.Infrastructure.Migrations;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Singleton
public class DatabaseMigrationManager {

    private final DataSource dataSource;

    @Inject
    public DatabaseMigrationManager(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void run() {
        createTables();
        applyConstraints();
    }

    private void createTables() {
        executeSql("""
                CREATE TABLE IF NOT EXISTS customer (
                    id          UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
                    first_name  VARCHAR(255)                NOT NULL,
                    last_name   VARCHAR(255)                NOT NULL,
                    email       VARCHAR(255)                NOT NULL,
                    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                    updated_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
                );
                """);

        executeSql("""
                CREATE TABLE IF NOT EXISTS product (
                    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                    name        VARCHAR(255)    NOT NULL,
                    sku         VARCHAR(100)    NOT NULL,
                    price       NUMERIC(12, 2)  NOT NULL,
                    stock       INT             NOT NULL DEFAULT 0,
                    category    VARCHAR(100)
                );
                """);

        executeSql("""
                CREATE TABLE IF NOT EXISTS orders (
                    id              UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
                    customer_id     UUID,
                    shipment_id     UUID,
                    status          VARCHAR(50)                 NOT NULL,
                    total_amount    NUMERIC(12, 2),
                    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
                );
                """);

        executeSql("""
                CREATE TABLE IF NOT EXISTS order_item (
                    id          UUID            PRIMARY KEY DEFAULT gen_random_uuid(),
                    order_id    UUID            NOT NULL,
                    product_id  UUID            NOT NULL,
                    quantity    INT             NOT NULL,
                    unit_price  NUMERIC(12, 2)  NOT NULL
                );
                """);

        executeSql("""
                CREATE TABLE IF NOT EXISTS shipment (
                    id              UUID                        PRIMARY KEY DEFAULT gen_random_uuid(),
                    tracking_code   VARCHAR(255)                NOT NULL,
                    carrier         VARCHAR(255)                NOT NULL,
                    status          VARCHAR(100)                NOT NULL,
                    created_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
                    updated_at      TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now()
                );
                """);
    }

    private void applyConstraints() {
        // customer
        executeSql("CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_email ON customer(email);");

        // product
        executeSql("CREATE UNIQUE INDEX IF NOT EXISTS uq_product_sku ON product(sku);");
        applyConstraintIfNotExists("chk_product_price", "product",
                "ALTER TABLE product ADD CONSTRAINT chk_product_price CHECK (price >= 0)");
        applyConstraintIfNotExists("chk_product_stock", "product",
                "ALTER TABLE product ADD CONSTRAINT chk_product_stock CHECK (stock >= 0)");

        // orders
        applyConstraintIfNotExists("fk_orders_shipment", "orders",
                "ALTER TABLE orders ADD CONSTRAINT fk_orders_shipment FOREIGN KEY (shipment_id) REFERENCES shipment(id)");
        applyConstraintIfNotExists("fk_orders_customer", "orders",
                "ALTER TABLE orders ADD CONSTRAINT fk_orders_customer FOREIGN KEY (customer_id) REFERENCES customer(id)");
        applyConstraintIfNotExists("chk_orders_total", "orders",
                "ALTER TABLE orders ADD CONSTRAINT chk_orders_total CHECK (total_amount >= 0)");

        // order_item
        applyConstraintIfNotExists("fk_order_item_order", "order_item",
                "ALTER TABLE order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE");
        applyConstraintIfNotExists("fk_order_item_product", "order_item",
                "ALTER TABLE order_item ADD CONSTRAINT fk_order_item_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE RESTRICT");
        applyConstraintIfNotExists("chk_order_item_quantity", "order_item",
                "ALTER TABLE order_item ADD CONSTRAINT chk_order_item_quantity CHECK (quantity > 0)");
        applyConstraintIfNotExists("chk_order_item_price", "order_item",
                "ALTER TABLE order_item ADD CONSTRAINT chk_order_item_price CHECK (unit_price >= 0)");

        // shipment
        executeSql("CREATE UNIQUE INDEX IF NOT EXISTS uq_shipment_tracking ON shipment(tracking_code);");
    }

    private void applyConstraintIfNotExists(String constraintName, String table, String alterSql) {
        String checkSql = """
            SELECT 1 FROM information_schema.table_constraints
            WHERE constraint_name = ? AND table_name = ?
            """;

        try (Connection connection = dataSource.getConnection();
             PreparedStatement check = connection.prepareStatement(checkSql)) {

            check.setString(1, constraintName);
            check.setString(2, table);

            try (ResultSet rs = check.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement alter = connection.prepareStatement(alterSql)) {
                        alter.execute();
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to apply constraint: " + constraintName, e);
        }
    }

    private void executeSql(String sql) {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Migration failed:\n" + sql, e);
        }
    }
}
