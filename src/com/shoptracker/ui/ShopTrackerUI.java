package com.shoptracker.ui;

import com.shoptracker.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ShopTrackerUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private final InventoryService inventoryService;
    private final User currentUser;
    private final DefaultTableModel tableModel;
    private final JTable table;

    // Column indexes
    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_QTY = 2;
    private static final int COL_PRICE = 3;
    private static final int COL_PLUS = 4;
    private static final int COL_MINUS = 5;

    public ShopTrackerUI(User user) {
        this.currentUser = user;
        this.inventoryService = InventoryService.getInstance();

        // Seed default stock once
        inventoryService.seedDefaultStockIfEmpty();

        setTitle("Shop Tracker – Inventory Manager");
        setSize(1000, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        // Header
        JPanel header = new JPanel();
        header.setBackground(UIConstants.BG_COLOR);
        JLabel headerLabel = new JLabel(
                "Logged in as: " + user.getFullName() + " (" + user.getRole() + ")"
        );
        headerLabel.setFont(UIConstants.FONT_BOLD);
        header.add(headerLabel);
        add(header, BorderLayout.NORTH);

        // Table with + / - columns
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Quantity", "Price (€)", "+", "-"}, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                // Make table cells non-editable; interaction via mouse clicks
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(25);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getColumnModel().getColumn(COL_PLUS).setMaxWidth(50);
        table.getColumnModel().getColumn(COL_MINUS).setMaxWidth(50);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(Color.WHITE);
        add(scrollPane, BorderLayout.CENTER);

        // Mouse listener for + / - clicks
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (row < 0 || col < 0) {
                    return;
                }
                if (col == COL_PLUS) {
                    handleAdjustQuantity(row, +1);
                } else if (col == COL_MINUS) {
                    handleAdjustQuantity(row, -1);
                }
            }
        });

        // Bottom button panel
        JPanel buttons = new JPanel();
        buttons.setBackground(UIConstants.BG_COLOR);

        JButton addBtn = UIConstants.createModernButton("Add Product");
        JButton delBtn = UIConstants.createModernButton("Delete Product");
        JButton refBtn = UIConstants.createModernButton("Refresh");
        JButton userMgmtBtn = UIConstants.createModernButton("Manage Users");
        JButton logBtn = UIConstants.createModernButton("View Activity Log");
        JButton outBtn = UIConstants.createModernButton("Logout");

        buttons.add(addBtn);
        buttons.add(delBtn);
        buttons.add(refBtn);
        buttons.add(userMgmtBtn);
        buttons.add(logBtn);
        buttons.add(outBtn);

        add(buttons, BorderLayout.SOUTH);

        // Permissions
        AccessControl ac = AccessControl.getInstance();
        if (!ac.canManageStock(user)) {
            addBtn.setEnabled(false);
            delBtn.setEnabled(false);
        }
        if (!ac.canManageUsers(user)) {
            userMgmtBtn.setEnabled(false);
            logBtn.setEnabled(false);
        }

        // Actions
        addBtn.addActionListener(e -> addProduct());
        delBtn.addActionListener(e -> deleteProduct());
        refBtn.addActionListener(e -> refreshTable());
        outBtn.addActionListener(e -> logout());
        userMgmtBtn.addActionListener(e -> openUserManagement());
        logBtn.addActionListener(e -> showActivityLog());

        refreshTable();
    }

    private void handleAdjustQuantity(int row, int delta) {
        String id = (String) tableModel.getValueAt(row, COL_ID);
        int currentQty = (int) tableModel.getValueAt(row, COL_QTY);

        if (delta < 0 && currentQty == 0) {
            JOptionPane.showMessageDialog(this,
                    "Stock cannot go below zero.",
                    "Quantity Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = inventoryService.adjustQuantity(currentUser, id, delta);
        if (!success) {
            JOptionPane.showMessageDialog(this,
                    "Could not adjust quantity (product missing or below zero).",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        refreshTable();
    }

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
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
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
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getQuantity(),
                    p.getPrice(),
                    "     +",
                    "     -"
            });
        }
    }

    private void logout() {
        dispose();
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }

    private void openUserManagement() {
        new UserManagementUI(currentUser).setVisible(true);
    }

    private void showActivityLog() {
        ActivityLogService logService = ActivityLogService.getInstance();
        java.util.List<String> entries = logService.getEntries();

        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No activity logged yet.",
                    "Activity Log",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        JList<String> list = new JList<>(entries.toArray(new String[0]));
        list.setFont(UIConstants.FONT_REGULAR);

        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setPreferredSize(new Dimension(700, 300));

        JOptionPane.showMessageDialog(this,
                scrollPane,
                "Activity Log",
                JOptionPane.INFORMATION_MESSAGE);
    }
}
