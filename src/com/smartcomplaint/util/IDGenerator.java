package com.smartcomplaint.util;

import java.time.Year;
import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {
    private static final AtomicInteger counter = new AtomicInteger(100);

    public static String generateComplaintId(int sequenceNumber) {
        int currentYear = Year.now().getValue();
        return String.format("CMP%d%04d", currentYear, sequenceNumber);
    }

    public static String generateNextId() {
        int next = counter.incrementAndGet();
        return generateComplaintId(next);
    }
}
