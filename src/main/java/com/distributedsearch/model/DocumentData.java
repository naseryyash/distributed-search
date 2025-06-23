package com.distributedsearch.model;

import java.util.HashMap;
import java.util.Map;

public class DocumentData {

    private final Map<String, Double> termToFrequency = new HashMap<>();

    public void addTermFrequency(String term, double frequency) {
        termToFrequency.put(term, frequency);
    }

    public double getTermFrequency(String term) {
        return termToFrequency.get(term);
    }

}
