package jp.igapyon.mikureadfile.core;

import java.util.LinkedHashSet;
import java.util.Set;

final class LineEndings {
    private LineEndings() {
    }

    static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    static Scan collectLineEndings(String text) {
        Scan scan = new Scan();
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if (ch == '\r') {
                if (index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    scan.values.add("crlf");
                    scan.finalNewline = index + 2 == text.length();
                    index++;
                } else {
                    scan.values.add("cr");
                    scan.finalNewline = index + 1 == text.length();
                }
            } else if (ch == '\n') {
                scan.values.add("lf");
                scan.finalNewline = index + 1 == text.length();
            }
        }
        return scan;
    }

    static String classifyLineEnding(Set<String> values) {
        if (values.size() == 0) {
            return "none";
        }
        if (values.size() > 1) {
            return "mixed";
        }
        return values.iterator().next();
    }

    static final class Scan {
        Set<String> values = new LinkedHashSet<String>();
        boolean finalNewline;
    }
}
