package com.shoptracker;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ActivityLogService {

    private static final ActivityLogService INSTANCE = new ActivityLogService();

    private final List<String> entries = new ArrayList<>();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private ActivityLogService() {
    }

    public static ActivityLogService getInstance() {
        return INSTANCE;
    }

    public synchronized void log(String message) {
        String ts = LocalDateTime.now().format(formatter);
        entries.add(ts + " - " + message);
    }

    public synchronized List<String> getEntries() {
        return Collections.unmodifiableList(new ArrayList<>(entries));
    }

    public synchronized int size() {
        return entries.size();
    }

    public synchronized void clear() {
        entries.clear();
    }
}
