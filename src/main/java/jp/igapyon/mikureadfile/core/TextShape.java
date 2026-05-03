package jp.igapyon.mikureadfile.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TextShape {
    public String text;
    public String bom;
    public String lineEnding;
    public boolean finalNewline;
    public List<String> lines = new ArrayList<String>();
    public List<Boolean> lineHadEnding = new ArrayList<Boolean>();
    public int logicalLineCount;

    public static TextShape shapeText(String decoded, String encoding, boolean utf8BomPresent) {
        TextShape shape = new TextShape();
        String text = decoded;
        if ("utf-8".equals(encoding) && (utf8BomPresent || (text.length() > 0 && text.charAt(0) == '\ufeff'))) {
            if (text.length() > 0 && text.charAt(0) == '\ufeff') {
                text = text.substring(1);
            }
            shape.bom = "utf-8";
        }
        LineEndingScan endings = collectLineEndings(text);
        shape.lineEnding = classifyLineEnding(endings.values);
        shape.finalNewline = endings.finalNewline;
        splitLogicalLines(text, shape.lines, shape.lineHadEnding);
        shape.logicalLineCount = text.length() == 0 ? 0 : shape.lines.size();
        shape.text = normalizeLineEndings(text);
        return shape;
    }

    public static String normalizeLineEndings(String text) {
        return text.replace("\r\n", "\n").replace("\r", "\n");
    }

    private static LineEndingScan collectLineEndings(String text) {
        LineEndingScan scan = new LineEndingScan();
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

    private static String classifyLineEnding(Set<String> values) {
        if (values.size() == 0) {
            return "none";
        }
        if (values.size() > 1) {
            return "mixed";
        }
        return values.iterator().next();
    }

    private static void splitLogicalLines(String text, List<String> lines, List<Boolean> lineHadEnding) {
        if (text.length() == 0) {
            return;
        }
        int start = 0;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if (ch == '\r' || ch == '\n') {
                lines.add(text.substring(start, index));
                lineHadEnding.add(Boolean.TRUE);
                if (ch == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                start = index + 1;
            }
        }
        if (start < text.length()) {
            lines.add(text.substring(start));
            lineHadEnding.add(Boolean.FALSE);
        }
    }

    private static class LineEndingScan {
        Set<String> values = new LinkedHashSet<String>();
        boolean finalNewline;
    }
}
