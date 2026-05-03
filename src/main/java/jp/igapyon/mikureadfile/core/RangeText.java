package jp.igapyon.mikureadfile.core;

public class RangeText {
    public String text;
    public int lineCount;
    public Integer endLine;
    public boolean eof;

    public static RangeText rangeText(TextShape shape, int startLine, int requestedLineCount) {
        RangeText result = new RangeText();
        if (startLine > shape.logicalLineCount) {
            result.text = "";
            result.lineCount = 0;
            result.endLine = null;
            result.eof = true;
            return result;
        }
        int startIndex = startLine - 1;
        int available = shape.logicalLineCount - startIndex;
        int lineCount = Math.min(requestedLineCount, available);
        result.lineCount = lineCount;
        result.endLine = Integer.valueOf(startLine + lineCount - 1);
        result.eof = lineCount < requestedLineCount;
        StringBuilder text = new StringBuilder();
        for (int index = startIndex; index < startIndex + lineCount; index++) {
            text.append(TextShape.normalizeLineEndings(shape.lines.get(index)));
            if (shape.lineHadEnding.get(index).booleanValue()) {
                text.append('\n');
            }
        }
        result.text = text.toString();
        return result;
    }
}
