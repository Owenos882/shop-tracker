package com.shoptracker.tests;

import com.shoptracker.ActivityLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ActivityLogServiceTest {

    private ActivityLogService logService;

    @BeforeEach
    void setUp() {
        logService = ActivityLogService.getInstance();
        logService.clear();
    }

    @Test
    void logsMessagesWithTimestamps() {
        logService.log("Test entry 1");
        logService.log("Test entry 2");

        assertEquals(2, logService.size());
        assertTrue(logService.getEntries().get(0).contains("Test entry 1"));
    }
}
