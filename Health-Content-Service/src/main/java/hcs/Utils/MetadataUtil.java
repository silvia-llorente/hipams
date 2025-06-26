package hcs.Utils;

import java.io.ByteArrayInputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

public class MetadataUtil {
    public String[] parseDG(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document d = builder.parse(is);
        String dg_id, title, type, abs, projC, desc;
        Element root = d.getDocumentElement();
        if (root.getElementsByTagName("Title").getLength() != 0) {
            Node n = root.getElementsByTagName("Title").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                title = n.getTextContent();
            } else title = null;
        } else title = null;
        if (root.getElementsByTagName("UUID").getLength() != 0) {
            Node n = root.getElementsByTagName("UUID").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                dg_id = n.getTextContent();
            } else dg_id = "-1";
        } else dg_id = "-1";
        if (root.getElementsByTagName("Type").getLength() != 0) {
            Node n = root.getElementsByTagName("Type").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                n = n.getAttributes().getNamedItem("existing_study_type");
                if (n != null) {
                    type = n.getNodeValue();
                }else type = null;
            } else type = null;
        } else type = null;
        if (root.getElementsByTagName("Abstract").getLength() != 0) {
            Node n = root.getElementsByTagName("Abstract").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                abs = n.getTextContent();
            } else abs = null;
        } else abs = null;
        if (root.getElementsByTagName("ProjectCentreName").getLength() != 0) {
            Node n = root.getElementsByTagName("ProjectCentreName").item(0);
            if (n.getParentNode().getNodeName().equals("ProjectCentre")) {
                projC = n.getTextContent();
            } else projC = null;
        }  else projC = null;
        if (root.getElementsByTagName("Description").getLength() != 0) {
            Node n = root.getElementsByTagName("Description").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetGroupMetadata")) {
                desc = n.getTextContent();
            } else desc = null;
        } else desc = null;
        //db.Modifiers.insertDG(dg_id,title,type,abs,projC,desc,dg.getOwner(),dg.getMpegfile().getId());
        String[] dg = new String[]{dg_id,title,type,abs,projC,desc};
        return dg;
    }
        
    public String[] parseDT(String xml) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new StringReader(xml));
        Document d = builder.parse(is);
        String dt_id, title, type, abs, projC, desc;
        Element root = d.getDocumentElement();
        if (root.getElementsByTagName("Title").getLength() != 0) {
            Node n = root.getElementsByTagName("Title").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                title = n.getTextContent();
            } else title = null;
        } else title = null;
        if (root.getElementsByTagName("UUID").getLength() != 0) {
            Node n = root.getElementsByTagName("UUID").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                dt_id = n.getTextContent();
            } else dt_id = "-1";
        } else dt_id = "-1";
        if (root.getElementsByTagName("Type").getLength() != 0) {
            Node n = root.getElementsByTagName("Type").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                n = n.getAttributes().getNamedItem("existing_study_type");
                if (n != null) {
                    type = n.getNodeValue();
                }else type = null;
            } else type = null;
        } else type = null;
        if (root.getElementsByTagName("Abstract").getLength() != 0) {
            Node n = root.getElementsByTagName("Abstract").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                abs = n.getTextContent();
            } else abs = null;
        } else abs = null;
        if (root.getElementsByTagName("ProjectCentreName").getLength() != 0) {
            Node n = root.getElementsByTagName("ProjectCentreName").item(0);
            if (n.getParentNode().getNodeName().equals("ProjectCentre")) {
                projC = n.getTextContent();
            } else projC = null;
        }  else projC = null;
        if (root.getElementsByTagName("Description").getLength() != 0) {
            Node n = root.getElementsByTagName("Description").item(0);
            if (n.getParentNode().getNodeName().equals("DatasetMetadata")) {
                desc = n.getTextContent();
            } else desc = null;
        } else desc = null;
        String[] dg = new String[]{dt_id,title,type,abs,projC,desc};
        return dg;
    }
    
    public String[] parsePatient(String xml)
            throws ParserConfigurationException, IOException, SAXException {

        // 1) Parse XML amb DOM i namespace-aware
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new ByteArrayInputStream(xml.getBytes("UTF-8"))));
        
        // Namespace de FHIR
        final String FHIR_NS = "http://hl7.org/fhir";

        // 2) Troba MedicalMetadata i en llegeix l’atribut resourceType
        Element medMeta = (Element) doc.getElementsByTagName("MedicalMetadata").item(0);
        String resourceType = medMeta.getAttribute("resourceType");

        // 3) Dins de <fhir:Patient>:
        Element fhirPatient = (Element)
            medMeta.getElementsByTagNameNS(FHIR_NS, "Patient").item(0);

        // 3a) identifier
        Element idElem = (Element) fhirPatient.getElementsByTagNameNS(FHIR_NS, "identifier").item(0);
        String identifier = idElem.getAttribute("value");

        // 3b) name → family + given
        Element nameElem = (Element) fhirPatient.getElementsByTagNameNS(FHIR_NS, "name").item(0);
        String family = ((Element) nameElem.getElementsByTagNameNS(FHIR_NS, "family").item(0))
                            .getAttribute("value");
        String given  = ((Element) nameElem.getElementsByTagNameNS(FHIR_NS, "given").item(0))
                            .getAttribute("value");
        String fullName = family + ", " + given;

        // 3c) address → line, city, country
        Element addrElem = (Element) fhirPatient.getElementsByTagNameNS(FHIR_NS, "address").item(0);
        String line    = ((Element) addrElem.getElementsByTagNameNS(FHIR_NS, "line").item(0))
                            .getAttribute("value");
        String city    = ((Element) addrElem.getElementsByTagNameNS(FHIR_NS, "city").item(0))
                            .getAttribute("value");
        String country = ((Element) addrElem.getElementsByTagNameNS(FHIR_NS, "country").item(0))
                            .getAttribute("value");
        String address = line + ", " + city + ", " + country;

        // 4) Retornem els quatre camps
        return new String[] { resourceType, identifier, fullName, address };
    }
}
