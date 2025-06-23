package com.distributedsearch.search;

import com.distributedsearch.model.DocumentData;

import java.util.*;

public class Tfidf {

    public static double calculateTermFrequency(
            List<String> words, String term) {
        long count = 0;

        for (String word: words) {
            if (term.equalsIgnoreCase(word)) {
                count++;
            }
        }

        return (double) count / words.size();
    }
    
    public static DocumentData createDocumentData(
            List<String> words, Set<String> terms) {
        DocumentData docData = new DocumentData();

        for (String term: terms) {
            docData.addTermFrequency(
                    term,
                    calculateTermFrequency(words, term));
        }

        return docData;
    }

    private static double getInverseDocumentFrequency(
            String term, Map<String, DocumentData> documentResults) {
        double nt = 0;

        for (DocumentData docData: documentResults.values()) {
            if (docData.getTermFrequency(term) > 0.0) {
                nt++;
            }
        }

        return (nt == 0) ? 0: Math.log10(documentResults.size() / nt);
    }

    private static Map<String, Double> getTermToInverseDocumentFrequencyMap(
            Set<String> terms, Map<String, DocumentData> documentResults) {
        Map<String, Double> termToIdf = new HashMap<>();

        for (String term: terms) {
            termToIdf.put(term,
                    getInverseDocumentFrequency(term, documentResults));
        }

        return termToIdf;
    }

    private static double calculateDocumentScore(Set<String> terms,
                                                 DocumentData docData,
                                                 Map<String, Double> termToIdf) {
        double score = 0;

        for (String term: terms) {
            double termFreq = docData.getTermFrequency(term);
            double inverseTermFreq = termToIdf.get(term);
            score += termFreq * inverseTermFreq;
        }

        return score;
    }

    public static Map<Double, List<String>> getDocumentsSortedByScore(Set<String> terms,
                                                                      Map<String, DocumentData> documentResults) {
        TreeMap<Double, List<String>> scoreToDocs = new TreeMap<>();

        Map<String, Double> termsToIdf =
                getTermToInverseDocumentFrequencyMap(terms, documentResults);

        for (Map.Entry<String, DocumentData> entry:
                documentResults.entrySet()) {
            double score = calculateDocumentScore(terms, entry.getValue(), termsToIdf);
            scoreToDocs.computeIfAbsent(score, k -> new ArrayList<>())
                    .add(entry.getKey());
        }

        return scoreToDocs.descendingMap();
    }

}
