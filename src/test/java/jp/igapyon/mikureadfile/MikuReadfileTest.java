package jp.igapyon.mikureadfile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jp.igapyon.mikureadfile.core.MikuReadfile;
import jp.igapyon.mikureadfile.model.MikuReadfileResult;

public class MikuReadfileTest {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @TempDir
    Path tempDir;

    @Test
    public void readsMinimalUtf8Request() throws Exception {
        Files.write(tempDir.resolve("README.md"), "hello\n".getBytes(StandardCharsets.UTF_8));

        MikuReadfileResult result = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"README.md\"]}");

        assertTrue(result.ok);
        assertEquals(1, result.files.size());
        assertEquals("README.md", result.files.get(0).file);
        assertEquals("utf-8", result.files.get(0).encoding);
        assertNull(result.files.get(0).bom);
        assertEquals("lf", result.files.get(0).lineEnding);
        assertTrue(result.files.get(0).finalNewline);
        assertEquals(1, result.files.get(0).lines);
        assertNull(result.files.get(0).range);
        assertEquals("hello\n", result.files.get(0).text);
        assertEquals(1, result.summary.requestedFiles);
        assertEquals(1, result.summary.filesRead);
        assertEquals(0, result.summary.filesSkipped);
        assertEquals(0, result.summary.diagnostics);
    }

    @Test
    public void supportsShiftJisRulesAndPerFileOverride() throws Exception {
        Files.createDirectories(tempDir.resolve("src"));
        Files.write(tempDir.resolve("src/Legacy.java"), "こんにちは\n".getBytes(Charset.forName("Shift_JIS")));
        Files.write(tempDir.resolve("memo.md"), "メモ\n".getBytes(Charset.forName("Shift_JIS")));

        MikuReadfileResult extension = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"src/Legacy.java\"],"
                + "\"encoding\":{\"default\":\"utf-8\",\"extensions\":{\".java\":\"shift_jis\"}}}");
        assertTrue(extension.ok);
        assertEquals("shift_jis", extension.files.get(0).encoding);
        assertEquals("こんにちは\n", extension.files.get(0).text);

        MikuReadfileResult override = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[{\"path\":\"memo.md\",\"encoding\":\"shift_jis\"}],"
                + "\"encoding\":{\"default\":\"utf-8\",\"extensions\":{\".md\":\"utf-8\"}}}");
        assertTrue(override.ok);
        assertEquals("shift_jis", override.files.get(0).encoding);
        assertEquals("メモ\n", override.files.get(0).text);
    }

    @Test
    public void returnsRangeMetadataAndEofBehavior() throws Exception {
        Files.write(tempDir.resolve("notes.txt"), "a\r\nb\r\n".getBytes(StandardCharsets.UTF_8));

        MikuReadfileResult range = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[{\"path\":\"notes.txt\",\"range\":{\"startLine\":2,\"lineCount\":4}}]}");
        assertTrue(range.ok);
        assertEquals("b\n", range.files.get(0).text);
        assertEquals(2, range.files.get(0).range.startLine);
        assertEquals(1, range.files.get(0).range.lineCount);
        assertEquals(Integer.valueOf(2), range.files.get(0).range.endLine);
        assertTrue(range.files.get(0).range.eof);

        MikuReadfileResult eof = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[{\"path\":\"notes.txt\",\"range\":{\"startLine\":9,\"lineCount\":2}}]}");
        assertTrue(eof.ok);
        assertEquals("", eof.files.get(0).text);
        assertEquals(0, eof.files.get(0).range.lineCount);
        assertNull(eof.files.get(0).range.endLine);
        assertTrue(eof.files.get(0).range.eof);
    }

    @Test
    public void rejectsInvalidPathsAndInvalidRequestShapes() throws Exception {
        MikuReadfileResult absolute = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"" + jsonPath(tempDir.resolve("README.md")) + "\"]}");
        assertFalse(absolute.ok);
        assertEquals("validation_error", absolute.diagnostics.get(0).code);

        MikuReadfileResult parent = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"../README.md\"]}");
        assertFalse(parent.ok);
        assertEquals("validation_error", parent.diagnostics.get(0).code);

        MikuReadfileResult unknown = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"README.md\"],\"extra\":true}");
        assertFalse(unknown.ok);
        assertEquals("extra", unknown.diagnostics.get(0).path);

        MikuReadfileResult invalidRange = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[{\"path\":\"README.md\",\"range\":{\"startLine\":1,\"lineCount\":0}}]}");
        assertFalse(invalidRange.ok);
        assertEquals("files.0.range.lineCount", invalidRange.diagnostics.get(0).path);
    }

    @Test
    public void skipsDirectoryInvalidUtf8BinarySymlinkAndTooBroadRoot() throws Exception {
        Files.createDirectory(tempDir.resolve("src"));
        MikuReadfileResult directory = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"src\"]}");
        assertFalse(directory.ok);
        assertEquals("not_file", directory.diagnostics.get(0).code);

        Files.write(tempDir.resolve("broken.txt"), new byte[] { (byte) 0x80 });
        MikuReadfileResult broken = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"broken.txt\"]}");
        assertFalse(broken.ok);
        assertEquals("decode_error", broken.diagnostics.get(0).code);

        Files.write(tempDir.resolve("binary.dat"), new byte[] { 0x61, 0x00, 0x62 });
        MikuReadfileResult binary = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"binary.dat\"]}");
        assertFalse(binary.ok);
        assertEquals("binary_file_skipped", binary.diagnostics.get(0).code);

        Files.write(tempDir.resolve("target.txt"), "target\n".getBytes(StandardCharsets.UTF_8));
        Files.createSymbolicLink(tempDir.resolve("link.txt"), tempDir.resolve("target.txt"));
        MikuReadfileResult symlink = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"link.txt\"]}");
        assertFalse(symlink.ok);
        assertEquals("symlink_skipped", symlink.diagnostics.get(0).code);

        MikuReadfileResult home = run("{\"version\":1,\"root\":\"" + jsonPath(Paths.get(System.getProperty("user.home"))) + "\",\"files\":[\"README.md\"]}");
        assertFalse(home.ok);
        assertEquals("root_too_broad", home.diagnostics.get(0).code);
    }

    @Test
    public void handlesBomEmptyFinalNewlineAndLineEndings() throws Exception {
        Files.write(tempDir.resolve("bom.txt"), "\ufeffhello".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("empty.txt"), new byte[0]);
        Files.write(tempDir.resolve("mixed.txt"), "a\r\nb\nc\rd".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("classic.txt"), "a\rb\r".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("nofinal.txt"), "no final".getBytes(StandardCharsets.UTF_8));

        MikuReadfileResult result = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"bom.txt\",\"empty.txt\",\"mixed.txt\",\"classic.txt\",\"nofinal.txt\"]}");
        assertTrue(result.ok);
        assertEquals("utf-8", result.files.get(0).bom);
        assertEquals("hello", result.files.get(0).text);
        assertEquals("", result.files.get(1).text);
        assertEquals(0, result.files.get(1).lines);
        assertEquals("none", result.files.get(1).lineEnding);
        assertEquals("mixed", result.files.get(2).lineEnding);
        assertEquals("a\nb\nc\nd", result.files.get(2).text);
        assertEquals("cr", result.files.get(3).lineEnding);
        assertTrue(result.files.get(3).finalNewline);
        assertEquals(2, result.files.get(3).lines);
        assertEquals("no final", result.files.get(4).text);
        assertFalse(result.files.get(4).finalNewline);
        assertEquals("none", result.files.get(4).lineEnding);
    }

    @Test
    public void enforcesLimitsAndPreservesDuplicatesAndPartialResults() throws Exception {
        Files.write(tempDir.resolve("a.txt"), "aaa".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("b.txt"), "bbb".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("same.txt"), "one\ntwo\n".getBytes(StandardCharsets.UTF_8));
        Files.write(tempDir.resolve("ok.txt"), "ok\n".getBytes(StandardCharsets.UTF_8));

        MikuReadfileResult fileLimit = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"a.txt\"],\"limits\":{\"maxFileBytes\":2}}");
        assertFalse(fileLimit.ok);
        assertEquals("max_file_bytes_exceeded", fileLimit.diagnostics.get(0).code);

        MikuReadfileResult totalLimit = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"a.txt\",\"b.txt\"],\"limits\":{\"maxTotalBytes\":5}}");
        assertFalse(totalLimit.ok);
        assertEquals(0, totalLimit.files.size());
        assertEquals("max_total_bytes_exceeded", totalLimit.diagnostics.get(0).code);

        MikuReadfileResult maxFiles = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"a.txt\",\"b.txt\"],\"limits\":{\"maxFiles\":1}}");
        assertFalse(maxFiles.ok);
        assertEquals(0, maxFiles.summary.requestedFiles);

        MikuReadfileResult duplicateReads = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":["
                + "{\"path\":\"same.txt\",\"range\":{\"startLine\":1,\"lineCount\":1}},"
                + "{\"path\":\"same.txt\",\"range\":{\"startLine\":2,\"lineCount\":1}}]}");
        assertTrue(duplicateReads.ok);
        assertEquals("one\n", duplicateReads.files.get(0).text);
        assertEquals("two\n", duplicateReads.files.get(1).text);

        MikuReadfileResult duplicateFailures = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"missing.txt\",\"missing.txt\"]}");
        assertFalse(duplicateFailures.ok);
        assertEquals(2, duplicateFailures.diagnostics.size());

        MikuReadfileResult partial = run("{\"version\":1,\"root\":\"" + jsonPath(tempDir) + "\",\"files\":[\"ok.txt\",\"missing.txt\"]}");
        assertFalse(partial.ok);
        assertEquals(1, partial.files.size());
        assertEquals("ok.txt", partial.files.get(0).file);
    }

    private MikuReadfileResult run(String json) throws IOException {
        JsonNode node = MAPPER.readTree(json);
        return MikuReadfile.runRequest(node);
    }

    private static String jsonPath(Path path) {
        return path.toAbsolutePath().toString().replace("\\", "\\\\");
    }
}
