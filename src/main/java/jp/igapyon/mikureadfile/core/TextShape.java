package jp.igapyon.mikureadfile.core;

import java.util.ArrayList;
import java.util.List;

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
        LineEndings.Scan endings = LineEndings.collectLineEndings(text);
        shape.lineEnding = LineEndings.classifyLineEnding(endings.values);
        shape.finalNewline = endings.finalNewline;
        LogicalLines.Split split = LogicalLines.splitLogicalLines(text);
        shape.lines = split.lines;
        shape.lineHadEnding = split.lineHadEnding;
        shape.logicalLineCount = text.length() == 0 ? 0 : shape.lines.size();
        shape.text = LineEndings.normalizeLineEndings(text);
        return shape;
    }
}
