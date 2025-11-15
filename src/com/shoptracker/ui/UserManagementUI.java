package com.shoptracker.ui;

import com.shoptracker.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UserManagementUI extends JFrame {

    private static final long serialVersionUID = 1L;

    // Safe constants (avoid “magic numbers”)
    private static final int DEFAULT_WIDTH = 700;
    private static final int DEFAULT_HEIGHT = 400;
    private static final int TABLE_ROW_HEIGHT = 24;
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 2;
    private static final int GRID_GAP = 5;

    private final transient User currentUser;
    private final transient UserRepository userRepo;
    private final transient UserService userService;

    private final DefaultTableModel tableModel;
    private final JTable table;

    public UserManagementUI(final User currentUser) {

        this.currentUser = currentUser;
        this.userRepo = UserRepository.getInstance();
        this.userService = new UserService(userRepo, AccessControl.getInstance());

        setTitle("User Management");
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(UIConstants.BG_COLOR);

        // Header
        JLabel header = new JLabel("User Management", SwingConstants.CENTER);
        header.setFont(UIConstants.FONT_BOLD);
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(header, BorderLayout.NORTH);

        // Table
        tableModel = new DefaultTableModel(
                new Object[]{"Username", "Full Name", "Email", "Role", "Active"}, 0);

        table = new JTable(tableModel);
        table.setFont(UIConstants.FONT_REGULAR);
        table.setRowHeight(TABLE_ROW_HEIGHT);
        table.getTableHeader().setFont(UIConstants.FONT_BOLD);

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel();
        buttons.setBackground(UIConstants.BG_COLOR);

        JButton addBtn = UIConstants.createModernButton("Add User");
        JButton delBtn = UIConstants.createModernButton("Delete User");
        JButton roleBtn = UIConstants.createModernButton("Change Role");
        JButton refreshBtn = UIConstants.createModernButton("Refresh");
        JButton closeBtn = UIConstants.createModernButton("Close");

        buttons.add(addBtn);
        buttons.add(delBtn);
        buttons.add(roleBtn);
        buttons.add(refreshBtn);
        buttons.add(closeBtn);

        add(buttons, BorderLayout.SOUTH);

        // Actions
        addBtn.addActionListener(e -> addUser());
        delBtn.addActionListener(e -> deleteUser());
        roleBtn.addActionListener(e -> changeRole());
        refreshBtn.addActionListener(e -> refreshTable());
        closeBtn.addActionListener(e -> dispose());

        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<User> users = userRepo.findAll();
        for (User u : users) {
            tableModel.addRow(new Object[]{
                    u.getUsername(),
                    u.getFullName(),
                    u.getEmail(),
                    u.getRole(),
                    u.isActive()
            });
        }
    }

    private void addUser() {
        JTextField usernameField = new JTextField();
        JTextField fullNameField = new JTextField();
        JTextField emailField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<Role> roleBox = new JComboBox<>(Role.values());

        JPanel panel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, GRID_GAP, GRID_GAP));
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Full Name:"));
        panel.add(fullNameField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add New User", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            String username = usernameField.getText();
            String fullName = fullNameField.getText();
            String email = emailField.getText();
            String password = new String(passwordField.getPassword());
            Role role = (Role) roleBox.getSelectedItem();

            User newUser = new User(username, password, fullName, email, role);
            boolean created = userService.createUser(currentUser, newUser);

            if (!created) {
                JOptionPane.showMessageDialog(this,
                        "Could not create user (maybe duplicate or no permission).",
                        "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                refreshTable();
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error creating user: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteUser() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a user row first.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(row, 0);

        if (username.equals(currentUser.getUsername())) {
            JOptionPane.showMessageDialog(this,
                    "You cannot delete your own account.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this, "Delete user " + username + "?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        boolean deleted = userService.deleteUser(currentUser, username);

        if (!deleted) {
            JOptionPane.showMessageDialog(this,
                    "Could not delete user (no permission or not found).",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } else {
            refreshTable();
        }
    }

    private void changeRole() {
        int row = table.getSelectedRow();

        if (row < 0) {
            JOptionPane.showMessageDialog(this,
                    "Select a user row first.",
                    "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String username = (String) tableModel.getValueAt(row, 0);

        JComboBox<Role> roleBox = new JComboBox<>(Role.values());

        int result = JOptionPane.showConfirmDialog(
                this, roleBox, "Select new role for " + username,
                JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        Role newRole = (Role) roleBox.getSelectedItem();

        try {
            userService.changeUserRole(currentUser, username, newRole);
            refreshTable();
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this,
                    "You do not have permission to change roles.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "User not found while changing role.",
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
