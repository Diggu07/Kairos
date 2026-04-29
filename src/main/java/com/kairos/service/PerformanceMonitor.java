package com.kairos.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PerformanceMonitor {
    
    private static PerformanceMonitor instance;
    private Map<String, Long> metrics;

    private PerformanceMonitor() {
        metrics = new ConcurrentHashMap<>();
    }

    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }

    public void startMeasurement(String key) {
        metrics.put(key + "_start", System.currentTimeMillis());
    }

    public long stopMeasurement(String key) {
        Long startTime = metrics.get(key + "_start");
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            metrics.put(key + "_duration", duration);
            System.out.println("[PERFORMANCE] " + key + " took " + duration + "ms");
            return duration;
        }
        return -1;
    }
    
    public long getMetric(String key) {
        return metrics.getOrDefault(key + "_duration", -1L);
    }
}
