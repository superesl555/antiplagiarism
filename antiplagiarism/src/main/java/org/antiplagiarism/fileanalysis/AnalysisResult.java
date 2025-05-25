package org.antiplagiarism.fileanalysis;

public record AnalysisResult(
        int paragraphs,
        int words,
        int characters,
        String cloudUrl
) {}
