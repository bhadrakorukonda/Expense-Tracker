-- Initial Database Schema
-- V1__Initial_Schema.sql

-- Create expenses table
CREATE TABLE IF NOT EXISTS expenses (
    id BIGSERIAL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    category VARCHAR(100),
    expense_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on expense_date for faster queries
CREATE INDEX idx_expense_date ON expenses(expense_date);

-- Create index on category for filtering
CREATE INDEX idx_category ON expenses(category);
