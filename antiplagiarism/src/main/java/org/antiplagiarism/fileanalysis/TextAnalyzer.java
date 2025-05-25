package org.antiplagiarism.fileanalysis;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextAnalyzer {

    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}’'-]+");

    private TextAnalyzer() {}

    public static AnalysisResult analyze(byte[] data) {
        String text = new String(data, StandardCharsets.UTF_8).trim();

        String[] paragraphs = text.split("(?m)(?:^\\s*$)+");
        int paragraphCount = paragraphs.length;

        int characterCount = text.length();

        Map<String, Integer> freq = new HashMap<>();
        Matcher matcher = WORD.matcher(text.toLowerCase());
        int wordCount = 0;
        while (matcher.find()) {
            String w = matcher.group();
            freq.merge(w, 1, Integer::sum);
            wordCount++;
        }

        StringBuilder sb = new StringBuilder();
        freq.forEach((word, cnt) -> {
            for (int i = 0; i < cnt; i++) {
                sb.append(word).append(' ');
            }
        });
        String cloudText = sb.toString().trim();
        if (cloudText.isEmpty()) {
            cloudText = " ";
        }

        String encoded = URLEncoder.encode(cloudText, StandardCharsets.UTF_8);
        String cloudUrl = "https://quickchart.io/wordcloud?text=" + encoded;

        return new AnalysisResult(paragraphCount, wordCount, characterCount, cloudUrl);
    }
}
