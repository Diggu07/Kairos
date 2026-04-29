package com.kairos.service;

import com.kairos.model.Entry;
import java.time.LocalDateTime;

public class MotivationService {
    private static MotivationService instance;

    public enum PromptType { ENCOURAGEMENT, WARNING, CELEBRATION, GENTLE_NUDGE }

    public static class MotivationPrompt {
        public String title;
        public String message;
        public PromptType type;
        public LocalDateTime generatedAt;
        public boolean dismissed;

        public MotivationPrompt(String title, String message, PromptType type) {
            this.title = title;
            this.message = message;
            this.type = type;
            this.generatedAt = LocalDateTime.now();
            this.dismissed = false;
        }
    }

    private MotivationService() {}

    public static synchronized MotivationService getInstance() {
        if (instance == null) {
            instance = new MotivationService();
        }
        return instance;
    }

    public MotivationPrompt generateMotivationalPrompt() {
        // Stub: based on user context
        return new MotivationPrompt(
            "Small steps beat giant leaps", 
            "Start with one small task today to build momentum.", 
            PromptType.GENTLE_NUDGE
        );
    }

    public int getMotivationScore() {
        // Stub: 0-100 score
        return 75;
    }
}
