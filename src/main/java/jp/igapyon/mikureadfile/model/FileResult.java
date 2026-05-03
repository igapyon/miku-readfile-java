package jp.igapyon.mikureadfile.model;

public class FileResult {
    public String file;
    public String encoding;
    public String bom;
    public String lineEnding;
    public boolean finalNewline;
    public long bytes;
    public int lines;
    public String modifiedTime;
    public RangeResult range;
    public String text;
}
