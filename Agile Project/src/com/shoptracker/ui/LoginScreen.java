package com.shoptracker.ui;

import com.shoptracker.*;
import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {
    private final UserRepository userRepo;
    private final AccessControl accessControl;

    public LoginScreen(UserRepository userRepo, AccessControl accessControl) {
        this.userRepo = userRepo;
        this.accessControl = accessControl;

        // ── Window setup ───────────────────────────────
        setTitle("Shop Tracker - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // ── Title label ────────────────────────────────
        JLabel title = new JLabel("Welcome to Shop Tracker", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // ── Form panel ─────────────────────────────────
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));

        JLabel userLbl = new JLabel("Username:");
        JLabel passLbl = new JLabel("Password:");
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        formPanel.add(userLbl);
        formPanel.add(userField);
        formPanel.add(passLbl);
        formPanel.add(passField);

        add(formPanel, BorderLayout.CENTER);

        // ── Message + Button panel ─────────────────────
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        JLabel msgLbl = new JLabel("", SwingConstants.CENTER);
        msgLbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JButton loginBtn = new JButton("Login");
        loginBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));

        bottomPanel.add(msgLbl, BorderLayout.CENTER);
        bottomPanel.add(loginBtn, BorderLayout.SOUTH);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 80, 20, 80));
        add(bottomPanel, BorderLayout.SOUTH);

        // ── Login action ───────────────────────────────
        loginBtn.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            User user = userRepo.findByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                msgLbl.setText("Login successful!");
                msgLbl.setForeground(new Color(0, 128, 0));

                JOptionPane.showMessageDialog(this,
                        "Welcome, " + user.getFullName() + " (" + user.getRole() + ")",
                        "Login Successful", JOptionPane.INFORMATION_MESSAGE);

                new ShopTrackerUI(user, accessControl).setVisible(true);
                dispose();
            } else {
                msgLbl.setText("Invalid username or password");
                msgLbl.setForeground(Color.RED);
            }
        });
    }

    // ── Main launcher ────────────────────────────────
    public static void main(String[] args) {
        UserRepository repo = new UserRepository();
        repo.save(new User("admin", "1234", "Alice Admin", "admin@shop.com", Role.ADMIN));
        repo.save(new User("manager", "5678", "Mark Manager", "manager@shop.com", Role.MANAGER));
        repo.save(new User("user", "0000", "Uma User", "user@shop.com", Role.USER));

        AccessControl ac = new AccessControl();
        SwingUtilities.invokeLater(() -> new LoginScreen(repo, ac).setVisible(true));
    }
}
