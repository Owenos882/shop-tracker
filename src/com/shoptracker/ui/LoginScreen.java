package com.shoptracker.ui;

import com.shoptracker.*;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    private final UserRepository userRepo;
    private final AccessControl accessControl;
    private final UserService userService;

    public LoginScreen() {
        this.userRepo = UserRepository.getInstance();
        this.accessControl = AccessControl.getInstance();
        this.userService = new UserService(userRepo, accessControl);

        seedUsersIfEmpty();

        setTitle("Shop Tracker - Login");
        setSize(440, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BG_COLOR);

        // Header
        JPanel header = new JPanel();
        header.setBackground(UIConstants.BG_COLOR);
        JLabel title = new JLabel("Shop Tracker Login");
        title.setFont(UIConstants.FONT_BOLD);
        title.setForeground(UIConstants.TEXT_COLOR);
        header.add(title);
        add(header, BorderLayout.NORTH);

        // Center form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        form.setBackground(UIConstants.PANEL_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLbl = new JLabel("Username:");
        JLabel passLbl = new JLabel("Password:");
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        userLbl.setFont(UIConstants.FONT_REGULAR);
        passLbl.setFont(UIConstants.FONT_REGULAR);

        // modern field styling
        userField.setFont(UIConstants.FONT_REGULAR);
        passField.setFont(UIConstants.FONT_REGULAR);

        Color fieldBg = Color.WHITE;
        Color fieldBorder = new Color(180, 180, 180);

        userField.setPreferredSize(new Dimension(200, 32));
        userField.setBackground(fieldBg);
        userField.setForeground(Color.BLACK);
        userField.setCaretColor(Color.BLACK);
        userField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fieldBorder, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        userField.setOpaque(true);

        passField.setPreferredSize(new Dimension(200, 32));
        passField.setBackground(fieldBg);
        passField.setForeground(Color.BLACK);
        passField.setCaretColor(Color.BLACK);
        passField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(fieldBorder, 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        passField.setOpaque(true);

        // Row 0 - username
        gbc.gridx = 0; gbc.gridy = 0;
        form.add(userLbl, gbc);
        gbc.gridx = 1;
        form.add(userField, gbc);

        // Row 1 - password
        gbc.gridx = 0; gbc.gridy = 1;
        form.add(passLbl, gbc);
        gbc.gridx = 1;
        form.add(passField, gbc);

        add(form, BorderLayout.CENTER);

        // Footer: message + buttons
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIConstants.BG_COLOR);

        JLabel msgLbl = new JLabel("", SwingConstants.CENTER);
        msgLbl.setForeground(Color.RED);
        msgLbl.setFont(UIConstants.FONT_REGULAR);

        JPanel buttonRow = new JPanel();
        buttonRow.setBackground(UIConstants.BG_COLOR);
        JButton loginBtn = UIConstants.createModernButton("Login");
        JButton forgotBtn = UIConstants.createModernButton("Forgot Password");

        buttonRow.add(loginBtn);
        buttonRow.add(forgotBtn);

        footer.add(msgLbl, BorderLayout.CENTER);
        footer.add(buttonRow, BorderLayout.SOUTH);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));

        add(footer, BorderLayout.SOUTH);

        // Actions
        loginBtn.addActionListener(evt -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            User user = userRepo.findByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                new ShopTrackerUI(user).setVisible(true);
                dispose();
            } else {
                msgLbl.setText("Invalid username or password");
            }
        });

        forgotBtn.addActionListener(evt -> handleForgotPassword());
    }

    private void handleForgotPassword() {
        String username = JOptionPane.showInputDialog(this, "Enter your username:");
        if (username == null || username.isBlank()) {
            return;
        }
        String email = JOptionPane.showInputDialog(this, "Enter your registered email:");
        if (email == null || email.isBlank()) {
            return;
        }

        try {
            String newPassword = userService.resetPassword(username, email);
            JOptionPane.showMessageDialog(this,
                    "Password reset successful.\nYour new temporary password is:\n\n" + newPassword,
                    "Password Reset",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "User not found: " + username,
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(this,
                    "Email does not match our records.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void seedUsersIfEmpty() {
        if (userRepo.findAll().isEmpty()) {
            userRepo.save(new User("admin", "1234", "Alice Admin", "admin@shop.com", Role.ADMIN));
            userRepo.save(new User("manager", "5678", "Mark Manager", "manager@shop.com", Role.MANAGER));
            userRepo.save(new User("user", "0000", "Uma User", "user@shop.com", Role.USER));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
