package jp.igapyon.mikureadfile.cli;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.igapyon.mikureadfile.core.MikuReadfile;
import jp.igapyon.mikureadfile.model.MikuReadfileResult;

public final class MikuReadfileCli {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MikuReadfileCli() {
    }

    public static void main(String[] args) {
        int code = run(args, System.in, System.out, System.err);
        System.exit(code);
    }

    public static int run(String[] args, InputStream in, PrintStream out, PrintStream err) {
        try {
            if (args.length == 1 && "--version".equals(args[0])) {
                out.print("miku-readfile " + packageVersion() + "\n");
                return 0;
            }
            if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
                out.print(HelpText.helpText());
                return 0;
            }
            if (args.length == 1 && args[0].startsWith("-")) {
                err.print("usage: miku-readfile [--version|--help]\n");
                return 2;
            }
            if (args.length > 0) {
                err.print("usage: miku-readfile [--version|--help]\n");
                return 2;
            }

            JsonNode request;
            try {
                request = MAPPER.readTree(readStdin(in));
            } catch (Exception ex) {
                err.print("malformed stdin: " + ex.getMessage() + "\n");
                return 2;
            }

            MikuReadfileResult result = MikuReadfile.runRequest(request);
            out.print(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            out.print("\n");
            return result.ok ? 0 : 1;
        } catch (Throwable ex) {
            err.print("unexpected runtime error: ");
            ex.printStackTrace(err);
            return 3;
        }
    }

    private static byte[] readStdin(InputStream in) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[8192];
        int read;
        while ((read = in.read(chunk)) >= 0) {
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    private static String packageVersion() {
        Package pkg = MikuReadfileCli.class.getPackage();
        String version = pkg == null ? null : pkg.getImplementationVersion();
        return version == null ? "0.5.0-SNAPSHOT" : version;
    }
}
