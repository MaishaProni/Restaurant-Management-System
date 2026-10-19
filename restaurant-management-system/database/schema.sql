-- Restaurant Management System - Database Schema
-- Run this script to create the database and tables

CREATE DATABASE IF NOT EXISTS restaurant_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE restaurant_db;

CREATE TABLE IF NOT EXISTS users (
    user_id   INT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(50)  UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role      ENUM('MANAGER','WAITER','KITCHEN','CASHIER') NOT NULL,
    active    BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS restaurant_tables (
    table_id     INT AUTO_INCREMENT PRIMARY KEY,
    table_number INT UNIQUE NOT NULL,
    capacity     INT DEFAULT 4,
    status       ENUM('AVAILABLE','OCCUPIED','NEEDS_SERVICE') DEFAULT 'AVAILABLE'
);

CREATE TABLE IF NOT EXISTS menu_categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS menu_items (
    item_id      INT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100) NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    category_id  INT,
    is_available BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (category_id) REFERENCES menu_categories(category_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS ingredients (
    ingredient_id       INT AUTO_INCREMENT PRIMARY KEY,
    name                VARCHAR(100) UNIQUE NOT NULL,
    current_stock       DECIMAL(10,3) NOT NULL DEFAULT 0,
    unit                VARCHAR(20) NOT NULL,
    low_stock_threshold DECIMAL(10,3) DEFAULT 0
);

CREATE TABLE IF NOT EXISTS recipe_items (
    recipe_item_id    INT AUTO_INCREMENT PRIMARY KEY,
    menu_item_id      INT NOT NULL,
    ingredient_id     INT NOT NULL,
    quantity_required DECIMAL(10,3) NOT NULL,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(item_id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS orders (
    order_id   INT AUTO_INCREMENT PRIMARY KEY,
    table_id   INT NOT NULL,
    waiter_id  INT NOT NULL,
    status     ENUM('PENDING','CONFIRMED','PREPARING','READY','SERVED','PAID','CANCELLED') DEFAULT 'PENDING',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id),
    FOREIGN KEY (waiter_id) REFERENCES users(user_id)
);

CREATE TABLE IF NOT EXISTS order_items (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id      INT NOT NULL,
    menu_item_id  INT NOT NULL,
    quantity      INT NOT NULL,
    unit_price    DECIMAL(10,2) NOT NULL,
    notes         VARCHAR(255),
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (menu_item_id) REFERENCES menu_items(item_id)
);

CREATE TABLE IF NOT EXISTS bills (
    bill_id        INT AUTO_INCREMENT PRIMARY KEY,
    order_id       INT UNIQUE NOT NULL,
    subtotal       DECIMAL(10,2) NOT NULL,
    tax_amount     DECIMAL(10,2) NOT NULL,
    service_charge DECIMAL(10,2) NOT NULL,
    grand_total    DECIMAL(10,2) NOT NULL,
    generated_at   DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id INT AUTO_INCREMENT PRIMARY KEY,
    bill_id    INT UNIQUE NOT NULL,
    amount     DECIMAL(10,2) NOT NULL,
    method     ENUM('CASH','CARD','MOBILE') NOT NULL,
    paid_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (bill_id) REFERENCES bills(bill_id)
);

CREATE TABLE IF NOT EXISTS inventory_adjustments (
    adjustment_id   INT AUTO_INCREMENT PRIMARY KEY,
    ingredient_id   INT NOT NULL,
    manager_id      INT NOT NULL,
    quantity_changed DECIMAL(10,3) NOT NULL,
    reason          VARCHAR(255),
    adjusted_at     DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(ingredient_id),
    FOREIGN KEY (manager_id) REFERENCES users(user_id)
);
