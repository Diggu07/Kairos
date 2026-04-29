package com.kairos.service;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MotivationQuoteService {
    
    private List<String> quotes = Arrays.asList(
        "Focus on being productive instead of busy. - Tim Ferriss",
        "Amateurs sit and wait for inspiration, the rest of us just get up and go to work. - Stephen King",
        "It's not always that we need to do more but rather that we need to focus on less. - Nathan W. Morris",
        "The way to get started is to quit talking and begin doing. - Walt Disney"
    );

    public String getRandomQuote() {
        Random random = new Random();
        return quotes.get(random.nextInt(quotes.size()));
    }
}
