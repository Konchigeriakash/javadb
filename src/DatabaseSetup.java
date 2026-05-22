import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseSetup {
    private DatabaseSetup() {
    }

    public static void setup() throws SQLException {
        try (Connection connection = DatabaseManager.openConnection();
                Statement statement = connection.createStatement()) {
            resetOldSchemaIfNeeded(connection, statement);
            createTables(statement);
        }

        seedData();
    }

    private static void resetOldSchemaIfNeeded(Connection connection, Statement statement) throws SQLException {
        if (!hasTable(connection, "stock_transactions") && !hasColumn(connection, "products", "sku")) {
            return;
        }

        statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 0");
        statement.executeUpdate("DROP TABLE IF EXISTS audit_log");
        statement.executeUpdate("DROP TABLE IF EXISTS stock_transactions");
        statement.executeUpdate("DROP TABLE IF EXISTS sale_items");
        statement.executeUpdate("DROP TABLE IF EXISTS sales");
        statement.executeUpdate("DROP TABLE IF EXISTS purchase_order_items");
        statement.executeUpdate("DROP TABLE IF EXISTS purchase_orders");
        statement.executeUpdate("DROP TABLE IF EXISTS sales_order_items");
        statement.executeUpdate("DROP TABLE IF EXISTS sales_orders");
        statement.executeUpdate("DROP TABLE IF EXISTS inventory");
        statement.executeUpdate("DROP TABLE IF EXISTS products");
        statement.executeUpdate("DROP TABLE IF EXISTS customers");
        statement.executeUpdate("DROP TABLE IF EXISTS suppliers");
        statement.executeUpdate("DROP TABLE IF EXISTS categories");
        statement.executeUpdate("SET FOREIGN_KEY_CHECKS = 1");
    }

    private static boolean hasTable(Connection connection, String tableName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name = ?
                """)) {
            statement.setString(1, tableName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private static boolean hasColumn(Connection connection, String tableName, String columnName) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
                """)) {
            statement.setString(1, tableName);
            statement.setString(2, columnName);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        }
    }

    private static void createTables(Statement statement) throws SQLException {
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS categories (
                    category_id INT PRIMARY KEY AUTO_INCREMENT,
                    category_name VARCHAR(100) NOT NULL UNIQUE
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS suppliers (
                    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
                    supplier_name VARCHAR(150) NOT NULL UNIQUE
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS products (
                    product_id INT PRIMARY KEY AUTO_INCREMENT,
                    product_name VARCHAR(150) NOT NULL,
                    category_id INT NOT NULL,
                    supplier_id INT NOT NULL,
                    CONSTRAINT fk_products_category
                        FOREIGN KEY (category_id) REFERENCES categories(category_id),
                    CONSTRAINT fk_products_supplier
                        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS inventory (
                    product_id INT PRIMARY KEY,
                    quantity INT NOT NULL CHECK (quantity >= 0),
                    reorder_level INT NOT NULL CHECK (reorder_level >= 0),
                    CONSTRAINT fk_inventory_product
                        FOREIGN KEY (product_id) REFERENCES products(product_id)
                        ON DELETE CASCADE
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS customers (
                    customer_id INT PRIMARY KEY AUTO_INCREMENT,
                    customer_name VARCHAR(150) NOT NULL UNIQUE
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sales_orders (
                    sales_order_id INT PRIMARY KEY AUTO_INCREMENT,
                    customer_id INT NOT NULL,
                    CONSTRAINT fk_sales_orders_customer
                        FOREIGN KEY (customer_id) REFERENCES customers(customer_id)
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS sales_order_items (
                    sales_order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL CHECK (quantity > 0),
                    PRIMARY KEY (sales_order_id, product_id),
                    CONSTRAINT fk_sales_items_order
                        FOREIGN KEY (sales_order_id) REFERENCES sales_orders(sales_order_id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_sales_items_product
                        FOREIGN KEY (product_id) REFERENCES products(product_id)
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS purchase_orders (
                    purchase_order_id INT PRIMARY KEY AUTO_INCREMENT,
                    supplier_id INT NOT NULL,
                    CONSTRAINT fk_purchase_orders_supplier
                        FOREIGN KEY (supplier_id) REFERENCES suppliers(supplier_id)
                )
                """);
        statement.executeUpdate("""
                CREATE TABLE IF NOT EXISTS purchase_order_items (
                    purchase_order_id INT NOT NULL,
                    product_id INT NOT NULL,
                    quantity INT NOT NULL CHECK (quantity > 0),
                    PRIMARY KEY (purchase_order_id, product_id),
                    CONSTRAINT fk_purchase_items_order
                        FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders(purchase_order_id)
                        ON DELETE CASCADE,
                    CONSTRAINT fk_purchase_items_product
                        FOREIGN KEY (product_id) REFERENCES products(product_id)
                )
                """);
    }

    private static void seedData() throws SQLException {
        try (Connection connection = DatabaseManager.openConnection();
                Statement count = connection.createStatement();
                ResultSet resultSet = count.executeQuery("SELECT COUNT(*) FROM products")) {
            if (resultSet.next() && resultSet.getInt(1) > 0) {
                return;
            }
        }

        try (Connection connection = DatabaseManager.openConnection()) {
            connection.setAutoCommit(false);
            try {
                insert(connection, "INSERT INTO categories(category_name) VALUES (?)", "Electronics");
                insert(connection, "INSERT INTO categories(category_name) VALUES (?)", "Stationery");
                insert(connection, "INSERT INTO categories(category_name) VALUES (?)", "Groceries");

                insert(connection, "INSERT INTO suppliers(supplier_name) VALUES (?)", "TechSource Distributors");
                insert(connection, "INSERT INTO suppliers(supplier_name) VALUES (?)", "OfficePro Wholesale");
                insert(connection, "INSERT INTO suppliers(supplier_name) VALUES (?)", "DailyMart Supply");

                insert(connection,
                        "INSERT INTO products(product_name, category_id, supplier_id) VALUES (?, ?, ?)",
                        "Wireless Keyboard", 1, 1);
                insert(connection,
                        "INSERT INTO products(product_name, category_id, supplier_id) VALUES (?, ?, ?)",
                        "Optical Mouse", 1, 1);
                insert(connection,
                        "INSERT INTO products(product_name, category_id, supplier_id) VALUES (?, ?, ?)",
                        "A4 Notebook", 2, 2);
                insert(connection,
                        "INSERT INTO products(product_name, category_id, supplier_id) VALUES (?, ?, ?)",
                        "Premium Tea 500g", 3, 3);

                insert(connection, "INSERT INTO inventory(product_id, quantity, reorder_level) VALUES (?, ?, ?)", 1, 30,
                        10);
                insert(connection, "INSERT INTO inventory(product_id, quantity, reorder_level) VALUES (?, ?, ?)", 2, 40,
                        15);
                insert(connection, "INSERT INTO inventory(product_id, quantity, reorder_level) VALUES (?, ?, ?)", 3,
                        100, 25);
                insert(connection, "INSERT INTO inventory(product_id, quantity, reorder_level) VALUES (?, ?, ?)", 4, 18,
                        20);

                insert(connection, "INSERT INTO customers(customer_name) VALUES (?)", "Aarav Stores");
                insert(connection, "INSERT INTO customers(customer_name) VALUES (?)", "Neha Retail");

                insert(connection, "INSERT INTO sales_orders(customer_id) VALUES (?)", 1);
                insert(connection,
                        "INSERT INTO sales_order_items(sales_order_id, product_id, quantity) VALUES (?, ?, ?)",
                        1, 2, 3);

                insert(connection, "INSERT INTO purchase_orders(supplier_id) VALUES (?)", 1);
                insert(connection,
                        "INSERT INTO purchase_order_items(purchase_order_id, product_id, quantity) VALUES (?, ?, ?)",
                        1, 1, 5);

                connection.commit();
            } catch (SQLException ex) {
                connection.rollback();
                throw ex;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static void insert(Connection connection, String sql, Object... values) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }
}
