package jp.igapyon.mikureadfile.docs;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import jp.igapyon.mikureadfile.cli.HelpText;

class DocumentationSyncTest {
    @Test
    void readmeSpecParityAndHelpSharePublicContractPointers() throws Exception {
        String readme = read("README.md");
        String spec = read("docs/miku-readfile-cli-spec.md");
        String parity = read("docs/cli-json-parity.md");
        String help = HelpText.helpText();

        assertTrue(readme.contains("docs/miku-readfile-cli-spec.md"));
        assertTrue(readme.contains("docs/cli-json-parity.md"));
        assertTrue(readme.contains("docs/parity-golden/"));
        assertTrue(readme.contains("java -jar target/miku-readfile.jar < request.json > result.json"));
        assertTrue(readme.contains("target/miku-readfile.jar"));
        assertTrue(readme.contains("node scripts/parity-check.mjs"));

        assertTrue(spec.contains("java -jar target/miku-readfile.jar < request.json > result.json"));
        assertTrue(spec.contains("Request Validation"));
        assertTrue(spec.contains("Range Reads"));
        assertTrue(spec.contains("BOM Handling"));
        assertTrue(spec.contains("Line Endings"));
        assertTrue(spec.contains("node scripts/parity-check.mjs"));
        assertTrue(spec.contains("docs/parity-golden/"));
        assertTrue(spec.contains("docs/cli-json-parity.md"));

        assertTrue(parity.contains("Documentation Synchronization"));
        assertTrue(parity.contains("docs/miku-readfile-cli-spec.md"));
        assertTrue(parity.contains("docs/parity-golden/"));
        assertTrue(parity.contains("java -jar target/miku-readfile.jar --help"));
        assertTrue(parity.contains("Shift_JIS decoder behavior"));

        assertTrue(help.contains("miku-readfile"));
        assertTrue(help.contains("Usage:"));
        assertTrue(help.contains("Safety:"));
        assertTrue(help.contains("docs/miku-readfile-cli-spec.md"));
        assertTrue(help.contains("docs/cli-json-parity.md"));
    }

    @Test
    void distributionAssemblyIncludesRuntimeFacingDocuments() throws Exception {
        String assembly = read("src/assembly/dist.xml");

        assertTrue(assembly.contains("<source>${project.build.directory}/${project.build.finalName}.jar</source>"));
        assertTrue(assembly.contains("<destName>miku-readfile.jar</destName>"));
        assertTrue(assembly.contains("<source>README.md</source>"));
        assertTrue(assembly.contains("<source>LICENSE</source>"));
        assertTrue(assembly.contains("<source>docs/miku-readfile-cli-spec.md</source>"));
        assertTrue(assembly.contains("<source>docs/cli-json-parity.md</source>"));
    }

    private static String read(String path) throws Exception {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
