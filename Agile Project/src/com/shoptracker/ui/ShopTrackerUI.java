package com.shoptracker.ui;

import com.shoptracker.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ShopTrackerUI extends JFrame {
    private final InventoryService inventoryService;
    private final User currentUser;
    private final DefaultTableModel tableModel;
    private final AccessControl accessControl;

    public ShopTrackerUI(User user, AccessControl accessControl) {
        this.currentUser = user;
        this.accessControl = accessControl;
        this.inventoryService = new InventoryService(accessControl);

        // ── Window setup ────────────────────────────────
        setTitle("Shop Tracker – Inventory Manager");
        setSize(750, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ── Header panel ────────────────────────────────
        JLabel headerLabel = new JLabel(
                "Logged in as: " + user.getFullName() + " (" + user.getRole() + ")",
                SwingConstants.CENTER);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        headerLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(headerLabel, BorderLayout.NORTH);

        // ── Table setup ─────────────────────────────────
        tableModel = new DefaultTableModel(new Object[]{"ID", "Name", "Quantity", "Price (€)"}, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(24);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Button panel ────────────────────────────────
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton addBtn = new JButton("➕ Add Product");
        JButton deleteBtn = new JButton("🗑️ Delete Product");
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton logoutBtn = new JButton("🚪 Logout");

        Dimension btnSize = new Dimension(140, 35);
        for (JButton b : new JButton[]{addBtn, deleteBtn, refreshBtn, logoutBtn}) {
            b.setPreferredSize(btnSize);
            b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        }

        buttonPanel.add(addBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);
        buttonPanel.add(logoutBtn);
        add(buttonPanel, BorderLayout.SOUTH);

        // ── Role-based permissions ───────────────────────
        if (!accessControl.canManageStock(user)) {
            addBtn.setEnabled(false);
            deleteBtn.setEnabled(false);
        }

        // ── Button actions ──────────────────────────────
        addBtn.addActionListener(e -> addProduct());
        deleteBtn.addActionListener(e -> deleteProduct());
        refreshBtn.addActionListener(e -> refreshTable());
        logoutBtn.addActionListener(e -> logout());

        // ── Final touches ───────────────────────────────
        refreshTable();
    }

    // ── Functional methods ─────────────────────────────
    private void addProduct() {
        try {
            String id = JOptionPane.showInputDialog(this, "Enter Product ID:");
            if (id == null) return;
            String name = JOptionPane.showInputDialog(this, "Enter Product Name:");
            if (name == null) return;
            int qty = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Quantity:"));
            double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Enter Price (€):"));
            inventoryService.addProduct(currentUser, new Product(id, name, qty, price));
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteProduct() {
        String id = JOptionPane.showInputDialog(this, "Enter Product ID to Delete:");
        if (id != null) {
            inventoryService.removeProduct(currentUser, id);
            refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Product> products = inventoryService.getAllProducts();
        for (Product p : products) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getQuantity(), p.getPrice()});
        }
    }

    private void logout() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Logout Confirmation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Go back to the login screen — shared inventory persists automatically
            UserRepository repo = new UserRepository();
            repo.save(new User("admin", "1234", "Alice Admin", "admin@shop.com", Role.ADMIN));
            repo.save(new User("manager", "5678", "Mark Manager", "manager@shop.com", Role.MANAGER));
            repo.save(new User("user", "0000", "Uma User", "user@shop.com", Role.USER));
            SwingUtilities.invokeLater(() -> new LoginScreen(repo, new AccessControl()).setVisible(true));
        }
    }

 	}

