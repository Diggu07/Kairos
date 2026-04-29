package com.kairos.service;

import com.kairos.model.DetectedIntent;
import com.kairos.model.Entry.EntryType;
import com.kairos.model.Entry.Priority;
import com.kairos.util.DateTimeExtractor;

import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service for Natural Language Processing on User Input.
 */
public class IntentDetectionService {

    private static IntentDetectionService instance;

    private TokenizerModel tokenizerModel;
    private TokenNameFinderModel personModel;
    private TokenNameFinderModel dateModel;
    private TokenNameFinderModel locationModel;

    private IntentDetectionService() {
        // Attempt to load OpenNLP models. If missing, we fallback to regex rules.
        try {
            InputStream tokenStream = getClass().getResourceAsStream("/opennlp-models/en-token.bin");
            if (tokenStream != null) tokenizerModel = new TokenizerModel(tokenStream);

            InputStream personStream = getClass().getResourceAsStream("/opennlp-models/en-ner-person.bin");
            if (personStream != null) personModel = new TokenNameFinderModel(personStream);

            InputStream dateStream = getClass().getResourceAsStream("/opennlp-models/en-ner-date.bin");
            if (dateStream != null) dateModel = new TokenNameFinderModel(dateStream);

            InputStream locStream = getClass().getResourceAsStream("/opennlp-models/en-ner-location.bin");
            if (locStream != null) locationModel = new TokenNameFinderModel(locStream);
        } catch (Exception e) {
            System.err.println("[IntentDetectionService] Could not load OpenNLP models. Using Regex Fallbacks.");
        }
    }

    public static synchronized IntentDetectionService getInstance() {
        if (instance == null) {
            instance = new IntentDetectionService();
        }
        return instance;
    }

    public DetectedIntent detectIntent(String userInput) {
        DetectedIntent intent = new DetectedIntent();
        if (userInput == null || userInput.trim().isEmpty()) {
            return intent;
        }

        String lowerInput = userInput.toLowerCase();

        // 1. Keyword matching for priority detection
        detectPriority(lowerInput, intent);

        // 2. Intent classification (Type)
        detectEntryType(lowerInput, intent);

        // 3. Extract Date/Time (Reminder Time)
        LocalDateTime reminderTime = DateTimeExtractor.extractDateTime(lowerInput);
        if (reminderTime != null) {
            intent.setSuggestedReminderTime(reminderTime);
            // If it clearly has a time, it's highly likely a reminder
            intent.setSuggestedType(EntryType.REMINDER);
            intent.setConfidenceScore(intent.getConfidenceScore() + 0.3);
        }

        // 4. Tokenization & NER
        performNER(userInput, intent);

        // Populate fields
        intent.setExtractedTitle(generateTitle(userInput));
        intent.setExtractedContent(userInput);

        // Normalize confidence
        intent.setConfidenceScore(Math.min(1.0, intent.getConfidenceScore()));

        return intent;
    }

    private void detectPriority(String text, DetectedIntent intent) {
        if (text.contains("urgent") || text.contains("asap") || text.contains("critical")) {
            intent.setSuggestedPriority(Priority.HIGH);
            intent.setConfidenceScore(intent.getConfidenceScore() + 0.2);
        } else if (text.contains("important") || text.contains("soon")) {
            intent.setSuggestedPriority(Priority.MEDIUM);
        } else if (text.contains("later") || text.contains("someday")) {
            intent.setSuggestedPriority(Priority.LOW);
        }
    }

    private void detectEntryType(String text, DetectedIntent intent) {
        boolean taskMatcher = Pattern.compile("\\b(do|buy|schedule|finish|complete)\\b").matcher(text).find();
        boolean remMatcher = Pattern.compile("\\b(remind|alert|notify)\\b").matcher(text).find();
        boolean noteMatcher = Pattern.compile("\\b(remember|note|idea)\\b").matcher(text).find();

        if (remMatcher) {
            intent.setSuggestedType(EntryType.REMINDER);
            intent.setConfidenceScore(0.8);
        } else if (taskMatcher) {
            intent.setSuggestedType(EntryType.TASK);
            intent.setConfidenceScore(0.7);
        } else if (noteMatcher) {
            intent.setSuggestedType(EntryType.NOTE);
            intent.setConfidenceScore(0.6);
        } else {
            intent.setSuggestedType(EntryType.NOTE); // default
            intent.setConfidenceScore(0.3);
        }
    }

    private void performNER(String text, DetectedIntent intent) {
        if (tokenizerModel == null) return; // Fallback to regex only if OpenNLP not loaded

        try {
            TokenizerME tokenizer = new TokenizerME(tokenizerModel);
            String[] tokens = tokenizer.tokenize(text);

            if (personModel != null) {
                NameFinderME personFinder = new NameFinderME(personModel);
                Span[] nameSpans = personFinder.find(tokens);
                for (Span s : nameSpans) {
                    StringBuilder name = new StringBuilder();
                    for(int i = s.getStart(); i < s.getEnd(); i++) {
                        name.append(tokens[i]).append(" ");
                    }
                    intent.addEntity("PERSON", name.toString().trim());
                    intent.addTag(name.toString().trim());
                }
                personFinder.clearAdaptiveData();
            }

            if (locationModel != null) {
                NameFinderME locFinder = new NameFinderME(locationModel);
                Span[] locSpans = locFinder.find(tokens);
                for (Span s : locSpans) {
                    StringBuilder loc = new StringBuilder();
                    for(int i = s.getStart(); i < s.getEnd(); i++) {
                        loc.append(tokens[i]).append(" ");
                    }
                    intent.addEntity("LOCATION", loc.toString().trim());
                    intent.addTag(loc.toString().trim());
                }
                locFinder.clearAdaptiveData();
            }
        } catch (Exception e) {
            System.err.println("[IntentDetectionService] Failed NER: " + e.getMessage());
        }
    }

    private String generateTitle(String text) {
        // Take first 5 words or up to newline as title
        String[] words = text.split("\\s+");
        StringBuilder title = new StringBuilder();
        int max = Math.min(words.length, 5);
        for (int i = 0; i < max; i++) {
            title.append(words[i]).append(" ");
        }
        if (words.length > 5) title.append("...");
        return title.toString().trim();
    }
}
