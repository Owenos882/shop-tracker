package com.shoptracker.ui;

import com.shoptracker.InventoryService;
import com.shoptracker.InventoryEvent;
import com.shoptracker.Product;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Displays the full history of inventory events.
 * Read-only window.
 */
public final class InventoryHistoryUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final InventoryService inventoryService;
    private final JTextArea logArea;

    /**
     * Legacy fallback constructor.
     * (Keeps any old UI code compiling, but does NOT share the real inventory state.)
     */
    public InventoryHistoryUI() {
        this(InventoryService.getInstance());
    }

    /**
     * Preferred constructor — always call this version.
     * Ensures history matches the actual active InventoryService instance.
     */
    public InventoryHistoryUI(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        setTitle("Inventory History");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        // ---- Title ----
        JLabel title = new JLabel("Inventory Change Log", SwingConstants.CENTER);
        title.setFont(UIConstants.FONT_BOLD);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        // ---- Log output ----
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(UIConstants.FONT_REGULAR);
        logArea.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // ---- Close button ----
        JButton closeBtn = UIConstants.createModernButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(UIConstants.BG_COLOR);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        loadHistory();
    }

    /**
     * Loads and formats inventory events.
     */
    private void loadHistory() {
        List<InventoryEvent> events = inventoryService.getHistory();

        if (events.isEmpty()) {
            logArea.setText("No inventory changes recorded yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (InventoryEvent e : events) {

            String timestamp = e.getTimestamp().format(TIME_FORMAT);

            Product product = inventoryService.getProduct(e.getProductId());
            String productName = (product != null)
                    ? product.getName()
                    : ("Product " + e.getProductId());

            int delta = e.getDelta();
            String changeText;

            if (delta > 0) {
                changeText = "increased by " + delta;
            } else if (delta < 0) {
                changeText = "decreased by " + Math.abs(delta);
            } else {
                changeText = "was updated";
            }

            sb.append(timestamp)
              .append(" - Stock for ")
              .append(productName)
              .append(" (ID: ").append(e.getProductId()).append(") ")
              .append(changeText)
              .append(" (new quantity: ").append(e.getNewQuantity()).append(")")
              .append(System.lineSeparator());
        }

        logArea.setText(sb.toString());
        logArea.setCaretPosition(0); // Start at top
    }
}
