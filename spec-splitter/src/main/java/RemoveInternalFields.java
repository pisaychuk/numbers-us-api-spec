import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;

public class RemoveInternalFields {

    public static final String VALUE = "value";
    public static final String INTERNAL = "internal";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private XmlParser xmlParser;
    private Boolean internalApiDoc = Boolean.FALSE;


    public void removeInternalData(String readFilePath, String writeFilePath, Boolean internalApiDoc, Boolean addXmlDeclaration) throws Exception {
        this.internalApiDoc = internalApiDoc;
        xmlParser = new XmlParser(internalApiDoc, addXmlDeclaration);
        JsonNode root = readJsonFile(readFilePath);
        removeInternalFields(root.elements());
        ObjectWriter writer = objectMapper.writer(new DefaultPrettyPrinter());
        writer.writeValue(new File(writeFilePath), root);
    }

    private void removeInternalFields(Iterator<JsonNode> elements) {
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            if (next.has(INTERNAL) ) {
                if (internalApiDoc || !Boolean.parseBoolean(next.get(INTERNAL).asText())) {
                    ((ObjectNode) next).remove(INTERNAL);
                } else {
                    elements.remove();
                    continue;
                }
            }
            if (next instanceof ObjectNode) {
                inspectXml((ObjectNode) next);
            }
            removeInternalFields(next.elements());
        }
    }

    private void inspectXml(ObjectNode next) {
        if (next.has(VALUE)) {
            final JsonNode jsonNode = next.get(VALUE);
            final String externalXmlPayload = xmlParser.removeInternalFieldsFromXml(jsonNode.asText());
            if (externalXmlPayload != null) {
                next.put(VALUE, externalXmlPayload);
            }
        }
    }

    private JsonNode readJsonFile(String readFilePath) throws Exception {
        Reader reader = Files.newBufferedReader(Path.of(readFilePath));
        return objectMapper.readTree(reader);
    }
}
