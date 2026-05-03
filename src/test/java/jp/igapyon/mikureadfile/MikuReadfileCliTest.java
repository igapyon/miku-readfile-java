package jp.igapyon.mikureadfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.igapyon.mikureadfile.cli.MikuReadfileCli;

public class MikuReadfileCliTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void returnsPlainTextForHelpAndVersion() {
        CliResult help = run(new String[] { "--help" }, "");
        assertEquals(0, help.code);
        assertTrue(help.stdout.contains("miku-readfile"));
        assertTrue(help.stdout.contains("Usage:"));
        assertTrue(help.stdout.contains("Range request example:"));
        assertTrue(help.stdout.contains("Safety:"));
        assertEquals("", help.stderr);

        CliResult version = run(new String[] { "--version" }, "");
        assertEquals(0, version.code);
        assertTrue(version.stdout.startsWith("miku-readfile "));
        assertEquals("", version.stderr);

        CliResult shortHelp = run(new String[] { "-h" }, "");
        assertEquals(0, shortHelp.code);
        assertTrue(shortHelp.stdout.contains("miku-readfile -h"));
    }

    @Test
    public void returnsExitCode2ForMalformedStdinAndInvalidUsage() {
        CliResult malformed = run(new String[0], "{");
        assertEquals(2, malformed.code);
        assertEquals("", malformed.stdout);
        assertTrue(malformed.stderr.contains("malformed stdin:"));

        CliResult invalidUsage = run(new String[] { "--unknown" }, "");
        assertEquals(2, invalidUsage.code);
        assertEquals("", invalidUsage.stdout);
        assertTrue(invalidUsage.stderr.contains("usage: miku-readfile"));
    }

    @Test
    public void returnsJsonAndExitCode1ForExpectedFailures() throws Exception {
        CliResult result = run(new String[0], "{\"version\":1,\"root\":\".\",\"files\":[]}");
        assertEquals(1, result.code);
        assertEquals("", result.stderr);
        JsonNode json = MAPPER.readTree(result.stdout);
        assertEquals(false, json.get("ok").booleanValue());
        assertEquals("validation_error", json.get("diagnostics").get(0).get("code").textValue());
    }

    private static CliResult run(String[] args, String stdin) {
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();
        int code = MikuReadfileCli.run(args,
                new ByteArrayInputStream(stdin.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(stdout, true),
                new PrintStream(stderr, true));
        CliResult result = new CliResult();
        result.code = code;
        result.stdout = new String(stdout.toByteArray(), StandardCharsets.UTF_8);
        result.stderr = new String(stderr.toByteArray(), StandardCharsets.UTF_8);
        return result;
    }

    private static class CliResult {
        int code;
        String stdout;
        String stderr;
    }
}
