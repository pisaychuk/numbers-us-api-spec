import org.apache.xerces.dom.DeferredTextImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

public class XmlParser {

    public static final String INTERNAL = "internal";
    public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
    private final Boolean isInternalDoc;
    private final Boolean addXmlDeclaration;

    public XmlParser(Boolean isInternalDoc, Boolean addXmlDeclaration) {
        this.isInternalDoc = isInternalDoc;
        this.addXmlDeclaration = addXmlDeclaration;
    }

    public String removeInternalFieldsFromXml(String xmlString) {
            return Optional.ofNullable(getDocument(xmlString))
                    .map(Document::getDocumentElement)
                    .map(this::removeInternalFields)
                    .map(Node::getOwnerDocument)
                    .map(this::stringifyXml)
                    .orElse(null);

    }
    private Node removeInternalFields(Node node) {
        final NodeList childNodes = node.getChildNodes();
        for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {
            final Node item = childNodes.item(childIndex);
            final Node internalNode = Optional.of(item)
                    .map(Node::getAttributes)
                    .map(att -> att.getNamedItem(INTERNAL)).orElse(null);
            if (internalNode!= null) {
                if (isInternalDoc || !Boolean.parseBoolean(internalNode.getNodeValue())) {
                    item.getAttributes().removeNamedItem(INTERNAL);
                } else {
                    final Node nextSibling = item.getNextSibling();
                    node.removeChild(item);
                    if (nextSibling instanceof DeferredTextImpl) {
                        node.removeChild(nextSibling);
                    }
                    continue;
                }
            }
            removeInternalFields(item);
        }
        return node;
    }

    private Document getDocument(String xmlString) {
        try {
            if (xmlString == null || xmlString.equals("null")) {
                return null;
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder;
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlString)));
        }catch (Exception e) {
            e.printStackTrace();
            System.out.println(xmlString);// used sout here as far as we do not need to LOG to file
            System.exit(1);
        }
        return null;
    }

    private String stringifyXml(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            if (addXmlDeclaration) {
                writer.write(XML_DECLARATION);
            }
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString();
        }catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
