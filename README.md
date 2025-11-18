ShopTracker – Inventory & User Management System

A modern Java Swing desktop application for managing store inventory, user accounts, stock levels, and activity logs. Built with clean architecture, strong role-based access control, and SonarQube-safe code.

📌 Features
✔ User Authentication

Login screen with validation

Password reset via username + registered email

User roles: ADMIN, MANAGER, USER

✔ Role-Based Access Control

ADMIN/MANAGER:

Add / delete products

Update product quantity & price

Manage users

View full activity log

USER:

Cannot create or delete products

Cannot edit pricing

Can only adjust stock quantity (+/-)

✔ Inventory Management

Add, edit, delete products

Increase/decrease stock via + / – buttons

Search products (case-insensitive, partial match)

Low stock detection and restock suggestions

Full inventory history with timestamps

✔ User Management

Add new users

Delete users

Change roles

View and search all users

✔ Activity Logging

Records every major action:

Login attempts

User creation/deletion

Role changes

Inventory updates

Logged with timestamps using ActivityLogService
