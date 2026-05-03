package jp.igapyon.mikureadfile.core;

import jp.igapyon.mikureadfile.model.EffectiveFileRequest;
import jp.igapyon.mikureadfile.model.FileResult;
import jp.igapyon.mikureadfile.model.RangeResult;

public final class FileResultFactory {
    private FileResultFactory() {
    }

    public static FileResult createFileResult(EffectiveFileRequest file, String encoding, String decoded, boolean utf8BomPresent, long bytes, String modifiedTime) {
        TextShape shaped = TextShape.shapeText(decoded, encoding, utf8BomPresent);
        RangeText effectiveRange = file.range == null ? null : RangeText.rangeText(shaped, file.range.startLine, file.range.lineCount);
        FileResult result = new FileResult();
        result.file = file.path.replace(java.io.File.separatorChar, '/');
        result.encoding = encoding;
        result.bom = shaped.bom;
        result.lineEnding = shaped.lineEnding;
        result.finalNewline = shaped.finalNewline;
        result.bytes = bytes;
        result.lines = shaped.logicalLineCount;
        result.modifiedTime = modifiedTime;
        if (file.range != null && effectiveRange != null) {
            RangeResult range = new RangeResult();
            range.startLine = file.range.startLine;
            range.lineCount = effectiveRange.lineCount;
            range.endLine = effectiveRange.endLine;
            range.eof = effectiveRange.eof;
            result.range = range;
            result.text = effectiveRange.text;
        } else {
            result.range = null;
            result.text = shaped.text;
        }
        return result;
    }
}
