package jp.igapyon.mikureadfile.core;

import java.nio.file.Path;

public final class PathSecurity {
    private PathSecurity() {
    }

    public static boolean hasParentSegment(String filePath) {
        String[] segments = filePath.split("/");
        for (String segment : segments) {
            if ("..".equals(segment)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPathInsideOrSame(Path candidate, Path base) {
        Path normalizedCandidate = candidate.normalize();
        Path normalizedBase = base.normalize();
        return normalizedCandidate.equals(normalizedBase) || normalizedCandidate.startsWith(normalizedBase);
    }
}
