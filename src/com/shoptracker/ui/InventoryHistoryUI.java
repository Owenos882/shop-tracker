package com.shoptracker.ui;

import com.shoptracker.AccessControl;
import com.shoptracker.InventoryService;
import com.shoptracker.InventoryService.InventoryEvent;
import com.shoptracker.Product;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class InventoryHistoryUI extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final InventoryService inventoryService;
    private final JTextArea logArea;

    // Fallback no-arg constructor (won't share state, but keeps old code compiling)
    public InventoryHistoryUI() {
        this(new InventoryService(AccessControl.getInstance()));
    }

    // Preferred constructor: pass the real InventoryService from your main UI
    public InventoryHistoryUI(InventoryService inventoryService) {
        this.inventoryService = inventoryService;

        setTitle("Inventory History");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(UIConstants.BG_COLOR);
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel("Inventory Change Log", SwingConstants.CENTER);
        title.setFont(UIConstants.FONT_BOLD);
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(UIConstants.FONT_REGULAR);
        logArea.setBackground(Color.WHITE);

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        JButton closeBtn = UIConstants.createModernButton("Close");
        closeBtn.addActionListener(e -> dispose());

        JPanel bottom = new JPanel();
        bottom.setBackground(UIConstants.BG_COLOR);
        bottom.add(closeBtn);
        add(bottom, BorderLayout.SOUTH);

        loadHistory();
    }

    private void loadHistory() {
        List<InventoryEvent> events = inventoryService.getHistory();

        if (events.isEmpty()) {
            logArea.setText("No inventory changes recorded yet.");
            return;
        }

        StringBuilder sb = new StringBuilder();

        for (InventoryEvent e : events) {
            Product p = inventoryService.getProduct(e.getProductId());
            String name = (p != null) ? p.getName() : ("Product " + e.getProductId());

            String ts = e.getTimestamp().format(TIME_FORMAT);

            String changeText;
            int delta = e.getDelta();
            if (delta > 0) {
                changeText = "increased by " + delta;
            } else if (delta < 0) {
                changeText = "decreased by " + Math.abs(delta);
            } else {
                changeText = "was updated";
            }

            sb.append(ts)
              .append(" - Stock for ")
              .append(name)
              .append(" (ID: ").append(e.getProductId()).append(") ")
              .append(changeText)
              .append(" (new quantity: ").append(e.getNewQuantity()).append(")")
              .append(System.lineSeparator());
        }

        logArea.setText(sb.toString());
        logArea.setCaretPosition(0); // scroll to top
    }
}
