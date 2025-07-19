package org.openapitools.codegen.java;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openapitools.codegen.ClientOptInput;
import org.openapitools.codegen.DefaultGenerator;
import org.openapitools.codegen.config.CodegenConfigurator;
import org.openapitools.codegen.languages.JavaClientCodegen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the x-expandable vendor extension functionality.
 * Tests the complete code generation flow to ensure x-expandable fields 
 * are properly transformed to ExpandableField<Type> in generated Java code.
 */
public class ExpandableFieldIntegrationTest {

    @Test
    public void testExpandableFieldCodeGeneration(@TempDir Path tempDir) throws IOException {
        // Configure the code generator
        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("java")
                .setLibrary("okhttp-gson")
                .setInputSpec("src/test/resources/3_0/expandable-field-test.yaml")
                .setOutputDir(tempDir.toAbsolutePath().toString())
                .addAdditionalProperty("modelPackage", "com.example.model");

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(clientOptInput).generate();

        // Verify files were generated
        assertFalse(files.isEmpty(), "No files were generated");

        // Find and verify the Customer model
        File customerFile = findGeneratedFile(tempDir, "Customer.java");
        assertNotNull(customerFile, "Customer.java file was not generated");

        // Read the generated Customer model content
        String customerContent = Files.readString(customerFile.toPath());
        System.out.println("Generated Customer model content:");
        System.out.println(customerContent);

        // Verify x-expandable transformations
        assertExpandableFieldTransformation(customerContent, "account", "Account");
        assertExpandableFieldTransformation(customerContent, "subscription", "Subscription");
        assertExpandableFieldTransformation(customerContent, "defaultPaymentMethod", "PaymentMethod");
        
        // Verify array x-expandable transformation
        assertExpandableArrayTransformation(customerContent, "paymentMethods", "PaymentMethod");

        // Verify regular fields are not transformed
        assertTrue(customerContent.contains("String name"), 
                "Regular field 'name' should remain as String");
        assertTrue(customerContent.contains("String email"), 
                "Regular field 'email' should remain as String");

        // Verify ExpandableField import is added
        assertTrue(customerContent.contains("import ExpandableField;") || 
                   customerContent.contains("ExpandableField"), 
                   "ExpandableField import should be present");

        // Test the Subscription model (nested x-expandable)
        File subscriptionFile = findGeneratedFile(tempDir, "Subscription.java");
        assertNotNull(subscriptionFile, "Subscription.java file was not generated");

        String subscriptionContent = Files.readString(subscriptionFile.toPath());
        System.out.println("Generated Subscription model content:");
        System.out.println(subscriptionContent);

        // Verify nested x-expandable transformation
        assertExpandableFieldTransformation(subscriptionContent, "plan", "Plan");
    }

    @Test
    public void testExpandableFieldWithDifferentJavaGenerators(@TempDir Path tempDir) throws IOException {
        // Test with different Java generators to ensure compatibility
        String[] generators = {"java", "java-micronaut-client"};
        
        for (String generatorName : generators) {
            Path generatorOutputDir = tempDir.resolve(generatorName);
            Files.createDirectories(generatorOutputDir);

            final CodegenConfigurator configurator = new CodegenConfigurator()
                    .setGeneratorName(generatorName)
                    .setInputSpec("src/test/resources/3_0/expandable-field-test.yaml")
                    .setOutputDir(generatorOutputDir.toAbsolutePath().toString())
                    .addAdditionalProperty("modelPackage", "com.example.model");

            final ClientOptInput clientOptInput = configurator.toClientOptInput();
            DefaultGenerator generator = new DefaultGenerator();
            List<File> files = generator.opts(clientOptInput).generate();

            assertFalse(files.isEmpty(), "No files were generated for " + generatorName);

            // Basic verification that Customer model was generated with expandable fields
            File customerFile = findGeneratedFile(generatorOutputDir, "Customer.java");
            if (customerFile != null) {
                String content = Files.readString(customerFile.toPath());
                assertTrue(content.contains("ExpandableField<Account>") ||
                          content.contains("ExpandableField<Subscription>") ||
                          content.contains("ExpandableField<PaymentMethod>"),
                          "At least one ExpandableField should be present in " + generatorName);
            }
        }
    }

    @Test
    public void testEmptyExpandableExtensionIgnored(@TempDir Path tempDir) throws IOException {
        // Create a minimal spec with empty x-expandable extension
        String minimalSpec = "openapi: 3.0.0\n" +
            "info:\n" +
            "  title: Test API\n" +
            "  version: 1.0.0\n" +
            "paths:\n" +
            "  /test:\n" +
            "    get:\n" +
            "      responses:\n" +
            "        '200':\n" +
            "          description: Success\n" +
            "components:\n" +
            "  schemas:\n" +
            "    TestModel:\n" +
            "      type: object\n" +
            "      properties:\n" +
            "        normalField:\n" +
            "          type: string\n" +
            "        emptyExpandableField:\n" +
            "          type: string\n" +
            "          x-expandable: \"\"\n" +
            "        nullExpandableField:\n" +
            "          type: string\n" +
            "          x-expandable:\n";

        // Write the spec to a temporary file
        Path specFile = tempDir.resolve("empty-expandable-test.yaml");
        Files.writeString(specFile, minimalSpec);

        final CodegenConfigurator configurator = new CodegenConfigurator()
                .setGeneratorName("java")
                .setInputSpec(specFile.toAbsolutePath().toString())
                .setOutputDir(tempDir.resolve("output").toAbsolutePath().toString());

        final ClientOptInput clientOptInput = configurator.toClientOptInput();
        DefaultGenerator generator = new DefaultGenerator();
        List<File> files = generator.opts(clientOptInput).generate();

        // Find the TestModel
        File testModelFile = findGeneratedFile(tempDir.resolve("output"), "TestModel.java");
        assertNotNull(testModelFile, "TestModel.java should be generated");

        String content = Files.readString(testModelFile.toPath());
        
        // Verify empty x-expandable extensions are ignored
        assertTrue(content.contains("String normalField") || content.contains("private String normalField"),
                "Normal field should remain as String");
        assertTrue(content.contains("String emptyExpandableField") || content.contains("private String emptyExpandableField"),
                "Empty x-expandable field should remain as String");
        assertTrue(content.contains("String nullExpandableField") || content.contains("private String nullExpandableField"),
                "Null x-expandable field should remain as String");
        
        // Should not contain any ExpandableField references
        assertFalse(content.contains("ExpandableField"),
                "No ExpandableField should be generated for empty extensions");
    }

    /**
     * Helper method to verify that a field has been transformed to ExpandableField<Type>
     */
    private void assertExpandableFieldTransformation(String content, String fieldName, String expectedType) {
        String expectedFieldDeclaration = "ExpandableField<" + expectedType + "> " + fieldName;
        String expectedFieldDeclarationPrivate = "private ExpandableField<" + expectedType + "> " + fieldName;
        
        boolean foundTransformation = content.contains(expectedFieldDeclaration) || 
                                     content.contains(expectedFieldDeclarationPrivate);
        
        assertTrue(foundTransformation, 
                String.format(Locale.ROOT, "Field '%s' should be transformed to ExpandableField<%s>. " +
                            "Expected to find '%s' or '%s' in generated content.", 
                            fieldName, expectedType, expectedFieldDeclaration, expectedFieldDeclarationPrivate));
    }

    /**
     * Helper method to verify that an array field has been transformed to List<ExpandableField<Type>>
     */
    private void assertExpandableArrayTransformation(String content, String fieldName, String expectedType) {
        String expectedFieldDeclaration = "List<ExpandableField<" + expectedType + ">> " + fieldName;
        String expectedFieldDeclarationPrivate = "private List<ExpandableField<" + expectedType + ">> " + fieldName;
        
        boolean foundTransformation = content.contains(expectedFieldDeclaration) || 
                                     content.contains(expectedFieldDeclarationPrivate);
        
        assertTrue(foundTransformation, 
                String.format(Locale.ROOT, "Array field '%s' should be transformed to List<ExpandableField<%s>>. " +
                            "Expected to find '%s' or '%s' in generated content.", 
                            fieldName, expectedType, expectedFieldDeclaration, expectedFieldDeclarationPrivate));
    }

    /**
     * Helper method to find a generated file by name in the output directory
     */
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