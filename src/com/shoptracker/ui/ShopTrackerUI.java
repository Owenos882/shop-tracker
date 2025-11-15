package com.shoptracker.ui;

import com.shoptracker.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ShopTrackerUI extends JFrame {

	private static final long serialVersionUID = 1L;

	private final transient InventoryService inventoryService;
	private final transient User currentUser;
    private final DefaultTableModel tableModel;
    private final JTable table;

    private static final int COL_ID = 0;
    private static final int COL_NAME = 1;
    private static final int COL_QTY = 2;
    private static final int COL_PRICE = 3;
    private static final int COL_PLUS = 4;
    private static final int COL_MINUS = 5;

    public ShopTrackerUI(User user) {
        this.currentUser = user;
        this.inventoryService = InventoryService.getInstance();

        inventoryService.seedDefaultStockIfEmpty();

        setTitle("Shop Tracker – Inventory Manager");
        setSize(1000, 550);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        getContentPane().setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        // Header
        JLabel headerLabel = new JLabel(
                "Logged in as: " + user.getFullName() + " (" + user.getRole() + ")"
        );
        headerLabel.setFont(UIConstants.FONT_BOLD);
        JPanel header = new JPanel();
        header.setBackground(UIConstants.BG_COLOR);
        header.add(headerLabel);
        add(header, BorderLayout.NORTH);

        // Table setup
        tableModel = new DefaultTableModel(
                new Object[]{"ID", "Name", "Quantity", "Price (€)", "+", "-"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(25);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);
        table.getColumnModel().getColumn(COL_PLUS).setMaxWidth(50);
        table.getColumnModel().getColumn(COL_MINUS).setMaxWidth(50);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Quantity adjust listener
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());
                if (col == COL_PLUS) {
                    handleAdjustQuantity(row, +1);
                } else if (col == COL_MINUS) {
                    handleAdjustQuantity(row, -1);
                }
            }
        });

        // Buttons bottom
        JButton addBtn = UIConstants.createModernButton("Add Product");
        JButton delBtn = UIConstants.createModernButton("Delete Product");
        JButton refreshBtn = UIConstants.createModernButton("Refresh");
        JButton userMgmtBtn = UIConstants.createModernButton("Manage Users");
        JButton logBtn = UIConstants.createModernButton("View Activity Log");
        JButton outBtn = UIConstants.createModernButton("Logout");

        JPanel buttons = new JPanel();
        buttons.setBackground(UIConstants.BG_COLOR);
        buttons.add(addBtn);
        buttons.add(delBtn);
        buttons.add(refreshBtn);
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

        // Button actions
        addBtn.addActionListener(e -> addProduct());
        delBtn.addActionListener(e -> deleteProduct());
        refreshBtn.addActionListener(e -> refreshTable());
        outBtn.addActionListener(e -> logout());
        userMgmtBtn.addActionListener(e -> openUserManagement());
        logBtn.addActionListener(e -> showActivityLog());

        refreshTable();
    }

    private void handleAdjustQuantity(int row, int delta) {
        String id = (String) tableModel.getValueAt(row, COL_ID);

        boolean ok = inventoryService.adjustQuantity(currentUser, id, delta);
        if (!ok) {
            JOptionPane.showMessageDialog(
                    this,
                    "Could not adjust quantity.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
        refreshTable();
    }

    private void addProduct() {
        try {
            String id = JOptionPane.showInputDialog(this, "Product ID:");
            if (id == null) {
                return;
            }
            String name = JOptionPane.showInputDialog(this, "Product Name:");
            if (name == null) {
                return;
            }
            int qty = Integer.parseInt(JOptionPane.showInputDialog(this, "Quantity:"));
            double price = Double.parseDouble(JOptionPane.showInputDialog(this, "Price (€):"));

            inventoryService.addProduct(currentUser, new Product(id, name, qty, price));
            refreshTable();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
        }
    }

    private void deleteProduct() {
        String id = JOptionPane.showInputDialog(this, "Product ID to Delete:");
        if (id != null) {
            inventoryService.removeProduct(currentUser, id);
            refreshTable();
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Product p : inventoryService.getAllProducts()) {
            tableModel.addRow(new Object[]{
                    p.getId(),
                    p.getName(),
                    p.getQuantity(),
                    p.getPrice(),
                    "+",
                    "-"
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
        var entries = ActivityLogService.getInstance().getEntries();
        if (entries.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No activity logged yet.");
            return;
        }

        JList<String> list = new JList<>(entries.toArray(new String[0]));
        JScrollPane pane = new JScrollPane(list);
        pane.setPreferredSize(new Dimension(700, 300));

        JOptionPane.showMessageDialog(
                this,
                pane,
                "Activity Log",
                JOptionPane.INFORMATION_MESSAGE
        );
    }
}
