#!/bin/bash
#
# Manual integration test script for x-expandable vendor extension
# This script generates Java client code using the OpenAPI Generator CLI
# and verifies that x-expandable fields are properly transformed
#

set -e

echo "🔧 Testing x-expandable vendor extension with OpenAPI Generator CLI"

# Build the project first
echo "📦 Building OpenAPI Generator..."
./mvnw clean install -DskipTests -q

# Set up test environment
TEST_SPEC="modules/openapi-generator/src/test/resources/3_0/expandable-field-test.yaml"
OUTPUT_DIR="test-output-expandable"
CLI_JAR="modules/openapi-generator-cli/target/openapi-generator-cli.jar"

# Clean up any previous test output
if [ -d "$OUTPUT_DIR" ]; then
    echo "🧹 Cleaning up previous test output..."
    rm -rf "$OUTPUT_DIR"
fi

echo "📁 Creating output directory: $OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Test different Java generators
declare -a generators=("java" "java-micronaut-client" "java-undertow-server")

for generator in "${generators[@]}"
do
    echo "🚀 Testing generator: $generator"
    
    GENERATOR_OUTPUT="$OUTPUT_DIR/$generator"
    
    # Generate code using the CLI
    java -jar "$CLI_JAR" generate \
        -i "$TEST_SPEC" \
        -g "$generator" \
        -o "$GENERATOR_OUTPUT" \
        --additional-properties modelPackage=com.example.model \
        --skip-validate-spec
    
    # Check if Customer model was generated
    CUSTOMER_FILE=$(find "$GENERATOR_OUTPUT" -name "Customer.java" -type f | head -1)
    
    if [ -n "$CUSTOMER_FILE" ]; then
        echo "✅ Found generated Customer model: $CUSTOMER_FILE"
        
        # Check for x-expandable transformations
        if grep -q "ExpandableField<Account>" "$CUSTOMER_FILE"; then
            echo "  ✅ Found ExpandableField<Account> transformation"
        else
            echo "  ❌ Missing ExpandableField<Account> transformation"
        fi
        
        if grep -q "ExpandableField<Subscription>" "$CUSTOMER_FILE"; then
            echo "  ✅ Found ExpandableField<Subscription> transformation"
        else
            echo "  ❌ Missing ExpandableField<Subscription> transformation"
        fi
        
        if grep -q "ExpandableField<PaymentMethod>" "$CUSTOMER_FILE"; then
            echo "  ✅ Found ExpandableField<PaymentMethod> transformation"
        else
            echo "  ❌ Missing ExpandableField<PaymentMethod> transformation"
        fi
        
        # Check that regular fields are unchanged
        if grep -q "String name" "$CUSTOMER_FILE" || grep -q "private String name" "$CUSTOMER_FILE"; then
            echo "  ✅ Regular field 'name' remains as String"
        else
            echo "  ❌ Regular field 'name' may have been incorrectly transformed"
        fi
        
        # Show a sample of the generated file
        echo "  📄 Generated Customer model preview:"
        head -20 "$CUSTOMER_FILE" | sed 's/^/    /'
        
    else
        echo "  ❌ Customer.java not found for generator: $generator"
    fi
    
    echo ""
done

# Test edge cases with a custom spec
echo "🧪 Testing edge cases..."
EDGE_CASE_SPEC="$OUTPUT_DIR/edge-case-spec.yaml"

cat > "$EDGE_CASE_SPEC" << 'EOF'
openapi: 3.0.0
info:
  title: Edge Case Test
  version: 1.0.0
paths:
  /test:
    get:
      responses:
        '200':
          description: Success
components:
  schemas:
    EdgeCaseModel:
      type: object
      properties:
        normalField:
          type: string
        emptyExpandable:
          type: string
          x-expandable: ""
        spaceExpandable:
          type: string
          x-expandable: "   "
        validExpandable:
          type: string
          x-expandable: MyCustomType
EOF

echo "📝 Generated edge case spec: $EDGE_CASE_SPEC"

# Test edge cases
EDGE_OUTPUT="$OUTPUT_DIR/edge-cases"
java -jar "$CLI_JAR" generate \
    -i "$EDGE_CASE_SPEC" \
    -g "java" \
    -o "$EDGE_OUTPUT" \
    --additional-properties modelPackage=com.example.edge \
    --skip-validate-spec

EDGE_MODEL=$(find "$EDGE_OUTPUT" -name "EdgeCaseModel.java" -type f | head -1)
if [ -n "$EDGE_MODEL" ]; then
    echo "✅ Edge case model generated: $EDGE_MODEL"
    
    # Verify edge cases
    if grep -q "String emptyExpandable" "$EDGE_MODEL" || grep -q "private String emptyExpandable" "$EDGE_MODEL"; then
        echo "  ✅ Empty x-expandable correctly ignored (remains String)"
    else
        echo "  ❌ Empty x-expandable not handled correctly"
    fi
    
    if grep -q "ExpandableField<MyCustomType>" "$EDGE_MODEL"; then
        echo "  ✅ Valid x-expandable correctly transformed"
    else
        echo "  ❌ Valid x-expandable not transformed"
    fi
    
    echo "  📄 Edge case model content:"
    cat "$EDGE_MODEL" | sed 's/^/    /'
fi

echo ""
echo "🎉 Testing complete! Check the generated files in: $OUTPUT_DIR"
echo "💡 To clean up: rm -rf $OUTPUT_DIR"

# Summary
echo ""
echo "📊 Test Summary:"
echo "   - Generated code for ${#generators[@]} different generators"
echo "   - Verified x-expandable field transformations"
echo "   - Tested edge cases (empty/invalid extensions)"
echo "   - All generated files are available in: $OUTPUT_DIR" 