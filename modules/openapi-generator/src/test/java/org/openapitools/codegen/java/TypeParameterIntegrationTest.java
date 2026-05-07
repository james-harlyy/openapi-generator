package org.openapitools.codegen.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TypeParameterIntegrationTest {
    @Test
    public void testTypeParameterExtensionsCodeGeneration(@TempDir Path tempDir) throws IOException {
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("java")
                .setLibrary("okhttp-gson")
                .setInputSpec("src/test/resources/3_0/type-parameter-test.yaml")
                .setOutputDir(tempDir.toAbsolutePath().toString())
                .addAdditionalProperty("modelPackage", "com.example.model");

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(clientOptInput).generate();

        assertFalse(files.isEmpty(), "No files were generated");

        File baseAnalyticsFile = findGeneratedFile(tempDir, "BaseAnalytics.java");
        assertNotNull(baseAnalyticsFile, "BaseAnalytics.java file was not generated");

        String content = Files.readString(baseAnalyticsFile.toPath());
        assertTrue(content.contains("public class BaseAnalytics<T, R>"),
                "BaseAnalytics should declare generic type parameters");
        assertTrue(content.contains("private T analytics") || content.contains("T analytics"),
                "analytics field should use x-type-parameter type");
        assertTrue(content.contains("private List<R> results") || content.contains("List<R> results"),
                "results field should use x-type-parameter from array items");
    }

    private File findGeneratedFile(Path outputDir, String fileName) {
        try {
            return Files.walk(outputDir)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(fileName))
                    .map(Path::toFile)
                    .findFirst()
                    .orElse(null);
        } catch (IOException e) {
            return null;
        }
    }
}
