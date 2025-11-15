package com.shoptracker.ui;

import com.shoptracker.*;

import javax.swing.*;
import java.awt.*;

public class LoginScreen extends JFrame {

    private static final long serialVersionUID = 1L;

    // --- Instance UI fields ---
    private JTextField userField;
    private JPasswordField passField;
    private JLabel msgLabel;

    private final transient UserRepository userRepo;
    private final transient AccessControl accessControl;
    private final transient UserService userService;
    private static final String ERROR_TITLE = "Error";


    public LoginScreen() {
        this.userRepo = UserRepository.getInstance();
        this.accessControl = AccessControl.getInstance();
        this.userService = new UserService(userRepo, accessControl);

        seedUsersIfEmpty();

        setupWindow();
        buildHeader();
        buildForm();
        buildFooter();
    }

    // ---------------------------------------------------------
    // Window Setup
    // ---------------------------------------------------------
    private void setupWindow() {
        setTitle("Shop Tracker - Login");
        setSize(440, 320);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UIConstants.BG_COLOR);
    }

    // ---------------------------------------------------------
    // Header
    // ---------------------------------------------------------
    private void buildHeader() {
        JPanel header = new JPanel();
        header.setBackground(UIConstants.BG_COLOR);

        JLabel title = new JLabel("Shop Tracker Login");
        title.setFont(UIConstants.FONT_BOLD);
        title.setForeground(UIConstants.TEXT_COLOR);

        header.add(title);
        add(header, BorderLayout.NORTH);
    }

    // ---------------------------------------------------------
    // Form
    // ---------------------------------------------------------
    private void buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        form.setBackground(UIConstants.PANEL_COLOR);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLbl = new JLabel("Username:");
        JLabel passLbl = new JLabel("Password:");
        userLbl.setFont(UIConstants.FONT_REGULAR);
        passLbl.setFont(UIConstants.FONT_REGULAR);

        userField = new JTextField();
        passField = new JPasswordField();

        styleField(userField);
        styleField(passField);

        // Row 0
        gbc.gridx = 0;
        gbc.gridy = 0;
        form.add(userLbl, gbc);

        gbc.gridx = 1;
        form.add(userField, gbc);

        // Row 1
        gbc.gridx = 0;
        gbc.gridy = 1;
        form.add(passLbl, gbc);

        gbc.gridx = 1;
        form.add(passField, gbc);

        add(form, BorderLayout.CENTER);
    }

    private void styleField(JTextField field) {
        field.setFont(UIConstants.FONT_REGULAR);
        field.setPreferredSize(new Dimension(200, 32));
        field.setBackground(Color.WHITE);
        field.setForeground(Color.BLACK);
        field.setCaretColor(Color.BLACK);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setOpaque(true);
    }

    // ---------------------------------------------------------
    // Footer
    // ---------------------------------------------------------
    private void buildFooter() {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(UIConstants.BG_COLOR);

        msgLabel = new JLabel("", SwingConstants.CENTER);
        msgLabel.setForeground(Color.RED);
        msgLabel.setFont(UIConstants.FONT_REGULAR);

        JPanel buttonRow = new JPanel();
        buttonRow.setBackground(UIConstants.BG_COLOR);

        JButton loginBtn = UIConstants.createModernButton("Login");
        JButton forgotBtn = UIConstants.createModernButton("Forgot Password");

        loginBtn.addActionListener(evt -> handleLogin());
        forgotBtn.addActionListener(evt -> handleForgotPassword());

        buttonRow.add(loginBtn);
        buttonRow.add(forgotBtn);

        footer.add(msgLabel, BorderLayout.CENTER);
        footer.add(buttonRow, BorderLayout.SOUTH);

        footer.setBorder(BorderFactory.createEmptyBorder(10, 40, 20, 40));
        add(footer, BorderLayout.SOUTH);
    }

    // ---------------------------------------------------------
    // Logic
    // ---------------------------------------------------------
    private void handleLogin() {
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());

        User user = userRepo.findByUsername(username);

        if (user != null && user.getPassword().equals(password)) {
            msgLabel.setText("");
            new ShopTrackerUI(user).setVisible(true);
            dispose();
        } else {
            msgLabel.setText("Invalid username or password");
        }
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

            JOptionPane.showMessageDialog(
                    this,
                    "Password reset successful.\nYour new temporary password is:\n\n" + newPassword,
                    "Password Reset",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "User not found: " + username,
                    ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE
            );
        } catch (SecurityException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Email does not match our records.",
                    ERROR_TITLE,
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // ---------------------------------------------------------
    // Seed default users if repository empty
    // ---------------------------------------------------------
    private void seedUsersIfEmpty() {
        if (userRepo.findAll().isEmpty()) {
            userRepo.save(new User("admin", "1234", "Alice Admin", "admin@shop.com", Role.ADMIN));
            userRepo.save(new User("manager", "5678", "Mark Manager", "manager@shop.com", Role.MANAGER));
            userRepo.save(new User("user", "0000", "Uma User", "user@shop.com", Role.USER));
        }
    }

    // ---------------------------------------------------------
    // Main entry point
    // ---------------------------------------------------------
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen().setVisible(true));
    }
}
