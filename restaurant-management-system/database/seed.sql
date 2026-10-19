-- Seed data (passwords are BCrypt-hashed "password123")
-- Run this AFTER schema.sql

USE restaurant_db;

INSERT IGNORE INTO users (username, password_hash, full_name, role) VALUES
('manager',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice Manager',  'MANAGER'),
('waiter',   '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob Waiter',     'WAITER'),
('kitchen',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Chef Charlie',   'KITCHEN'),
('cashier',  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Diana Cashier',  'CASHIER');

-- Note: The DataSeeder.java will auto-create the above users on first run
-- Use username/password123 to log in

INSERT IGNORE INTO restaurant_tables (table_number, capacity) VALUES
(1,4),(2,4),(3,6),(4,2),(5,4),(6,8),(7,4),(8,2),(9,6),(10,4);

INSERT IGNORE INTO menu_categories (name) VALUES
('Appetizers'), ('Main Course'), ('Beverages'), ('Desserts');

INSERT IGNORE INTO menu_items (name, price, category_id, is_available) VALUES
('Spring Rolls',    4.99,  1, TRUE),
('Chicken Wings',   7.99,  1, TRUE),
('Grilled Chicken', 14.99, 2, TRUE),
('Pasta Carbonara', 12.99, 2, TRUE),
('Beef Burger',     11.99, 2, TRUE),
('Soft Drink',      1.99,  3, TRUE),
('Fresh Juice',     3.49,  3, TRUE),
('Chocolate Cake',  5.99,  4, TRUE);

INSERT IGNORE INTO ingredients (name, current_stock, unit, low_stock_threshold) VALUES
('Chicken Breast', 20.000, 'kg',  5.000),
('All-Purpose Flour', 15.000, 'kg',  3.000),
('Pasta',          10.000, 'kg',  2.000),
('Beef Patty',     8.000,  'kg',  2.000),
('Tomato Sauce',   5.000,  'ltr', 1.000),
('Cheese',         4.000,  'kg',  1.000),
('Eggs',           30.000, 'pcs', 10.000),
('Chocolate',      3.000,  'kg',  0.500),
('Cream',          2.000,  'ltr', 0.500),
('Spring Roll Wrapper', 50.000, 'pcs', 10.000);

-- Recipe: Spring Rolls uses wrapper + sauce
INSERT IGNORE INTO recipe_items (menu_item_id, ingredient_id, quantity_required) VALUES
(1, 10, 3.000), -- Spring Rolls: 3 wrappers
(1, 5,  0.100), -- Spring Rolls: 100ml sauce
(2, 1,  0.300), -- Chicken Wings: 300g chicken
(3, 1,  0.250), -- Grilled Chicken: 250g chicken
(4, 3,  0.200), -- Pasta Carbonara: 200g pasta
(4, 7,  2.000), -- Pasta Carbonara: 2 eggs
(4, 6,  0.050), -- Pasta Carbonara: 50g cheese
(5, 4,  0.200), -- Beef Burger: 200g beef
(5, 2,  0.100), -- Beef Burger: 100g flour (bun)
(8, 8,  0.100), -- Chocolate Cake: 100g chocolate
(8, 9,  0.100), -- Chocolate Cake: 100ml cream
(8, 2,  0.200); -- Chocolate Cake: 200g flour
