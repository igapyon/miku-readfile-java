package jp.igapyon.mikureadfile.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import jp.igapyon.mikureadfile.model.Diagnostic;

public final class Root {
    private Root() {
    }

    public static Check checkRoot(Path rootPath, String requestRoot) {
        if (Files.exists(rootPath) && !Files.isDirectory(rootPath)) {
            return Check.failure(Diagnostics.rootError("root_not_accessible", "root is not a directory", requestRoot));
        }
        if (!Files.exists(rootPath) || !Files.isReadable(rootPath)) {
            String code = Files.exists(rootPath) ? "root_not_accessible" : "root_not_found";
            String message = "root_not_found".equals(code) ? "root does not exist" : "root is not accessible";
            return Check.failure(Diagnostics.rootError(code, message, requestRoot));
        }
        Path realPath;
        try {
            realPath = rootPath.toRealPath();
        } catch (IOException ex) {
            return Check.failure(Diagnostics.rootError("root_not_accessible", "root is not accessible", requestRoot));
        }
        Path filesystemRoot = realPath.getRoot();
        Path home = Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize();
        try {
            home = home.toRealPath();
        } catch (IOException ex) {
            // Keep normalized home path when the platform cannot resolve it.
        }
        if ((filesystemRoot != null && realPath.equals(filesystemRoot)) || realPath.equals(home)) {
            return Check.failure(Diagnostics.rootError("root_too_broad", "root is too broad", requestRoot));
        }
        return Check.success(realPath);
    }

    public static final class Check {
        boolean ok;
        Path realPath;
        Diagnostic diagnostic;

        static Check success(Path realPath) {
            Check check = new Check();
            check.ok = true;
            check.realPath = realPath;
            return check;
        }

        static Check failure(Diagnostic diagnostic) {
            Check check = new Check();
            check.ok = false;
            check.diagnostic = diagnostic;
            return check;
        }
    }
}
