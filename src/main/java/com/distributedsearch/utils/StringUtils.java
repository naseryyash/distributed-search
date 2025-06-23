package com.distributedsearch.utils;

import java.util.*;

import static com.distributedsearch.utils.StringConstants.LINE_SPLIT_REGEX;

public class StringUtils {

    private static List<String> getWordsFromLine(String line) {
        return Arrays.asList(
                line.split(LINE_SPLIT_REGEX));
    }

    public static List<String> getWordsFromLines(List<String> lines) {
        List<String> words = new ArrayList<>();

        for (String line: lines) {
            words.addAll(getWordsFromLine(line));
        }

        return words;
    }

    public static Set<String> getUniqueWordsFromLine(String line) {
        return new HashSet<>(getWordsFromLine(line));
    }

}
