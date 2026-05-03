package jp.igapyon.mikureadfile.core;

import java.util.ArrayList;
import java.util.List;

final class LogicalLines {
    private LogicalLines() {
    }

    static Split splitLogicalLines(String text) {
        Split split = new Split();
        if (text.length() == 0) {
            return split;
        }
        int start = 0;
        for (int index = 0; index < text.length(); index++) {
            char ch = text.charAt(index);
            if (ch == '\r' || ch == '\n') {
                split.lines.add(text.substring(start, index));
                split.lineHadEnding.add(Boolean.TRUE);
                if (ch == '\r' && index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                start = index + 1;
            }
        }
        if (start < text.length()) {
            split.lines.add(text.substring(start));
            split.lineHadEnding.add(Boolean.FALSE);
        }
        return split;
    }

    static final class Split {
        List<String> lines = new ArrayList<String>();
        List<Boolean> lineHadEnding = new ArrayList<Boolean>();
    }
}
