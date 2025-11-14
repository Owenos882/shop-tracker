package com.shoptracker.ui;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.JButton;

public final class UIConstants {

    private UIConstants() {}

    public static final String FONT_FAMILY = "Segoe UI";

    public static final Color BG_COLOR = new Color(245, 245, 245);     // Light grey
    public static final Color PANEL_COLOR = new Color(255, 255, 255);  // White
    public static final Color ACCENT_COLOR = new Color(52, 120, 229);  // Modern blue
    public static final Color TEXT_COLOR = new Color(40, 40, 40);
    public static final Color BORDER_COLOR = new Color(200, 200, 200);

    public static final Font FONT_REGULAR = new Font(FONT_FAMILY, Font.PLAIN, 14);
    public static final Font FONT_BOLD = new Font(FONT_FAMILY, Font.BOLD, 16);

    public static JButton createModernButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FONT_REGULAR);
        button.setBackground(new Color(235, 235, 235));
        button.setForeground(TEXT_COLOR);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        button.setPreferredSize(new Dimension(150, 35));
        return button;
    }
}
