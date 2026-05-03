package jp.igapyon.mikureadfile.model;

import java.util.ArrayList;
import java.util.List;

public class MikuReadfileResult {
    public int version = 1;
    public boolean ok;
    public List<FileResult> files = new ArrayList<FileResult>();
    public Summary summary = new Summary();
    public List<Diagnostic> diagnostics = new ArrayList<Diagnostic>();
}
