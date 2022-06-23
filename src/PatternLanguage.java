import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Array;
import java.text.SimpleDateFormat;
import java.util.*;

public class PatternLanguage implements PlmlConstants, GexfConstants {

    private List<Pattern> patternList;
    private Map<String, Pattern> patternMap;
    private List<PatternLink> linkList;
    private int linkCount;

    public PatternLanguage() {
        this.patternList = new ArrayList<>();
        this.patternMap = new HashMap<>();
        this.linkList = new ArrayList<>();
        this.linkCount = 1000;
    }

    public void parseXmlFiles(SortedSet<File> files) {
        for (File file : files) {
            String fileName = file.getName();

            System.out.println("Processing file: " + fileName);
            int patternNumber = Integer.parseInt(fileName.substring(0, 3));
            Document dom = loadXmlDom(file);
            Node patternElement = dom.getElementsByTagName(PATTERN_TAG).item(0);

            parsePatternElement(patternNumber, dom, patternElement);
        }
        resolveTargetPatterns();
        mergeSimilarLinks();
        consolidateLinks();
        System.out.println("Finished loading pattern and link objects!");
    }

    private static Document loadXmlDom(File file) {
        Document domObject = null;

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            domObject = builder.parse(file);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(2);
        }
        return domObject;
    }

    private void parsePatternElement(int number, Document dom, Node patternElement) {
        NamedNodeMap patternAttributes = patternElement.getAttributes();
        String patternCollection = patternAttributes.getNamedItem(PATTERN_ATTR_COLLECTION).getNodeValue();
        String patternId = patternAttributes.getNamedItem(PATTERN_ATTR_ID).getNodeValue();
        String patternName = dom.getElementsByTagName(PATTERN_NAME_TAG).item(0).getNodeValue();
        if ((patternName == null) || patternName.isBlank()) {
            //System.err.println("ALERTA: pattern com nome nulo ou vazio: " + patternId);
            patternName = PatternNamesConstants.fromNumber(number);
        }
        String patternGroup = GroupConstants.fromNumber(number);
        String patternCategory = CategoryConstants.fromNumber(number);
        NodeList linkNodeList = dom.getElementsByTagName(PATTERN_LINK_TAG);
        Pattern currentPattern = new Pattern(number, patternId, patternName, patternGroup, patternCategory, patternCollection);

        this.patternList.add(currentPattern);
        this.patternMap.put(patternId, currentPattern);
        for (int i = 0; i < linkNodeList.getLength(); i++) {
            Node node = linkNodeList.item(i);
            NamedNodeMap attributes = node.getAttributes();
            Node attrType = attributes.getNamedItem(PATTERN_LINK_ATTR_TYPE);
            Node attrPatternId = attributes.getNamedItem(PATTERN_LINK_ATTR_PATTERNID);
            Node attrCollection = attributes.getNamedItem(PATTERN_LINK_ATTR_COLLECTION);
            String typeStr = null;
            LinkType type = null;
            String patternIdStr = null;
            String collectionStr = null;

            if (attrType != null) {
                typeStr = attrType.getNodeValue();
                type = LinkType.fromValue(typeStr);
                if (type == null) {
                    System.err.println("ALERTA: Encontrado pattern-link com type desconhecido: " + typeStr);
                }
            } else {
                System.err.println("ALERTA: Encontrado pattern-link sem atributo 'type'!");
            }
            if (attrPatternId != null) {
                patternIdStr = attrPatternId.getNodeValue();
            } else {
                System.err.println("ALERTA: Encontrado pattern-link sem atributo 'patternID'!");
            }
            if (attrCollection != null) {
                collectionStr = attrCollection.getNodeValue();
            }
            if ((collectionStr != null) && !collectionStr.equals(CollectionConstants.WELIE_COLLECTION)) {
                System.out.println("Encontrado pattern-link para outra coleção: " + patternIdStr + ", " + collectionStr);
            }
            //System.out.println("Inserindo pattern-link: " + type + ", " + patternIdStr + ", " + collectionStr);
            Integer linkNumber = Integer.valueOf(this.linkCount++);
            PatternLink patternLink = new PatternLink(linkNumber, type, patternIdStr, collectionStr);

            patternLink.setSource(currentPattern);
            this.linkList.add(patternLink);
        }
    }

    private void resolveTargetPatterns() {
        for (PatternLink link : this.linkList) {
            String targetPatternId = link.getPatternId();
            String targetPatternCollection = link.getCollection();

            if ((targetPatternCollection == null)
                    || targetPatternCollection.equals(CollectionConstants.WELIE_COLLECTION)) {
                Pattern targetPattern = this.patternMap.get(targetPatternId);

                if (targetPattern != null) {
                    link.setTarget(targetPattern);
                } else {
                    System.err.println("ALERTA: encontrou link para Welie em " + link.getSource().getId() + ", mas ID não encontrado: " + targetPatternId);
                }
            } else {

                // It is a link to a pattern in another collection.
                // System.out.println("Encontrou um link para pattern de outra coleção: " + targetPatternId + ", " + targetPatternCollection);
            }
        }
    }

    private void mergeSimilarLinks() {
        System.out.println("Consolidating pattern language links:");
        for (PatternLink link : this.linkList) {
            LinkType type = link.getType();

            if (type.equals(LinkType.IS_CONTAINED_BY)) {
                Pattern originalSource = link.getSource();
                Pattern originalTarget = link.getTarget();

                link.setSource(originalTarget);
                link.setTarget(originalSource);
                link.setPatternId(originalTarget.getId());
                link.setCollection(originalTarget.getCollection());
                link.setType(LinkType.CONTAINS);
            } else if (type.equals(LinkType.GENERALIZATION)) {
                Pattern originalSource = link.getSource();
                Pattern originalTarget = link.getTarget();

                link.setSource(originalTarget);
                link.setTarget(originalSource);
                link.setPatternId(originalTarget.getId());
                link.setCollection(originalTarget.getCollection());
                link.setType(LinkType.SPECIALIZATION);
            }
        }
    }

    // 1. Elimina links para outras coleções e os not-related
    // 2. Elimina os links duplicados
    private void consolidateLinks() {
        System.out.println("Consolidating pattern language links:");
        this.removeSpuriousLinks();
        this.removeDuplicatedLinks();
    }

    private void removeSpuriousLinks() {
        Set<PatternLink> toRemove = new HashSet<>();

        for (int i = 0; i < this.linkList.size(); i++) {
            PatternLink link = this.linkList.get(i);

            if (link.getTarget() == null) {
                if (link.getCollection().equals(CollectionConstants.WELIE_COLLECTION)) {
                    System.err.println("ALERTA: encontrou link Welie com target null: " + link.getSource().getId() + link.getPatternId());
                } else {
                    System.out.println("Encontrado link para outra coleção com target null: " + link.getSource().getId() + link.getPatternId() + ", " + link.getCollection());
                }
                toRemove.add(link);
            }
            if (link.getType().equals(LinkType.NOT_RELATED)) {
                System.out.println("Encontrado link com type 'not-related': " +  link.getPatternId());
                toRemove.add(link);
            }
        }
        System.out.println("Preparando para remover " + toRemove.size() + " links espúrios de um total de " + this.linkList.size());
        this.linkList.removeAll(toRemove);
        System.out.println("Total de links após remoção dos espúrios: " + this.linkList.size());
    }

    private void removeDuplicatedLinks() {
        Set<String> linkSignatures = new HashSet<>();
        Set<PatternLink> toRemove = new HashSet<>();

        for (PatternLink link : this.linkList) {
            String sourceId = link.getSource().getId();
            String targetId = link.getTarget().getId();
            String type = link.getType().getAttributeValue();
            String signature = sourceId + "_" + targetId + "_" + type;

            if (linkSignatures.contains(signature)) {
                System.out.println("Encontrado link duplicado: " + signature);
                toRemove.add(link);
            } else {
                linkSignatures.add(signature);
            }
        }
        System.out.println("Preparando para remover " + toRemove.size() + " links duplicados de um total de " + this.linkList.size());
        this.linkList.removeAll(toRemove);
        System.out.println("Total de links após remoção dos duplicados: " + this.linkList.size());
    }

    public Document generateGexfDom() {
        Document doc = null;
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            doc = docBuilder.newDocument();

            // Elemento raiz: gxef
            Element rootElement = doc.createElement(GEXF_TAG);
            rootElement.setAttribute(XMLNS_ATTR_NAME, XMLNS_ATTR_VALUE);
            rootElement.setAttribute(VERSION_ATTR_NAME, VERSION_ATTR_VALUE);
            rootElement.setAttribute(XSI_ATTR_NAME, XSI_ATTR_VALUE);
            rootElement.setAttribute(SCHEMA_ATTR_NAME, SCHEMA_ATTR_VALUE);
            doc.appendChild(rootElement);

            // Elemento meta
            Element metaElement = doc.createElement("meta");
            metaElement.setAttribute("lastmodifieddate", new SimpleDateFormat("YYYY-MM-dd").format(new Date()));
            Element creatorElement = doc.createElement("creator");
            creatorElement.setTextContent("Andrea Gnecco, Christian Mattjie, Diego Rosa, and Sofia Apuzzo");
            Element descriptionElement = doc.createElement("description");
            descriptionElement.setTextContent("A graph description for the Welie pattern language.");
            metaElement.appendChild(creatorElement);
            metaElement.appendChild(descriptionElement);
            rootElement.appendChild(metaElement);

            // Elemento graph
            Element graphElement = doc.createElement("graph");
            graphElement.setAttribute("defaultedgetype", "directed");
            graphElement.setAttribute("mode", "static");
            rootElement.appendChild(graphElement);

            // Elemento attributes dos nodos
            Element nodeAttributesElement = doc.createElement("attributes");
            nodeAttributesElement.setAttribute("class", "node");
            nodeAttributesElement.setAttribute("mode", "static");
            graphElement.appendChild(nodeAttributesElement);

            Element nodeNumberAttrElement = doc.createElement("attribute");
            nodeNumberAttrElement.setAttribute("id", "0");
            nodeNumberAttrElement.setAttribute("title", "number");
            nodeNumberAttrElement.setAttribute("type", "integer");
            nodeAttributesElement.appendChild(nodeNumberAttrElement);

            Element nodeIdAttrElement = doc.createElement("attribute");
            nodeIdAttrElement.setAttribute("id", "1");
            nodeIdAttrElement.setAttribute("title", "id");
            nodeIdAttrElement.setAttribute("type", "string");
            nodeAttributesElement.appendChild(nodeIdAttrElement);

            Element nodeNameAttrElement = doc.createElement("attribute");
            nodeNameAttrElement.setAttribute("id", "2");
            nodeNameAttrElement.setAttribute("title", "name");
            nodeNameAttrElement.setAttribute("type", "string");
            nodeAttributesElement.appendChild(nodeNameAttrElement);

            Element nodeGroupAttrElement = doc.createElement("attribute");
            nodeGroupAttrElement.setAttribute("id", "3");
            nodeGroupAttrElement.setAttribute("title", "group");
            nodeGroupAttrElement.setAttribute("type", "string");
            nodeAttributesElement.appendChild(nodeGroupAttrElement);

            Element nodeCategoryAttrElement = doc.createElement("attribute");
            nodeCategoryAttrElement.setAttribute("id", "4");
            nodeCategoryAttrElement.setAttribute("title", "category");
            nodeCategoryAttrElement.setAttribute("type", "string");
            nodeAttributesElement.appendChild(nodeCategoryAttrElement);

            Element nodeCollectionAttrElement = doc.createElement("attribute");
            nodeCollectionAttrElement.setAttribute("id", "5");
            nodeCollectionAttrElement.setAttribute("title", "collection");
            nodeCollectionAttrElement.setAttribute("type", "string");
            nodeAttributesElement.appendChild(nodeCollectionAttrElement);

            // Elemento attributes das arestas
            Element edgeAttributesElement = doc.createElement("attributes");
            edgeAttributesElement.setAttribute("class", "edge");
            edgeAttributesElement.setAttribute("mode", "static");
            graphElement.appendChild(edgeAttributesElement);

            Element edgeNumberAttrElement = doc.createElement("attribute");
            edgeNumberAttrElement.setAttribute("id", "0");
            edgeNumberAttrElement.setAttribute("title", "number");
            edgeNumberAttrElement.setAttribute("type", "integer");
            edgeAttributesElement.appendChild(edgeNumberAttrElement);

            Element edgeTypeAttrElement = doc.createElement("attribute");
            edgeTypeAttrElement.setAttribute("id", "1");
            edgeTypeAttrElement.setAttribute("title", "type");
            edgeTypeAttrElement.setAttribute("type", "string");
            edgeAttributesElement.appendChild(edgeTypeAttrElement);

            // Nodes
            Element nodesElement = doc.createElement("nodes");
            graphElement.appendChild(nodesElement);

            for (Pattern pattern : this.patternList) {
                Element nodeElement = doc.createElement("node");
                nodeElement.setAttribute("id", pattern.getId());
                nodeElement.setAttribute("label", pattern.getName());
                nodesElement.appendChild(nodeElement);
                Element nodeAttValues = doc.createElement("attvalues");
                nodeElement.appendChild(nodeAttValues);
                // number, id, name, group, category, collection
                Element nodeAttValue0 = doc.createElement("attvalue");
                nodeAttValue0.setAttribute("for", "0");
                nodeAttValue0.setAttribute("value", String.valueOf(pattern.getNumber()));
                nodeAttValues.appendChild(nodeAttValue0);
                Element nodeAttValue1 = doc.createElement("attvalue");
                nodeAttValue1.setAttribute("for", "1");
                nodeAttValue1.setAttribute("value", pattern.getId());
                nodeAttValues.appendChild(nodeAttValue1);
                Element nodeAttValue2 = doc.createElement("attvalue");
                nodeAttValue2.setAttribute("for", "2");
                nodeAttValue2.setAttribute("value", pattern.getName());
                nodeAttValues.appendChild(nodeAttValue2);
                Element nodeAttValue3 = doc.createElement("attvalue");
                nodeAttValue3.setAttribute("for", "3");
                nodeAttValue3.setAttribute("value", pattern.getGroup());
                nodeAttValues.appendChild(nodeAttValue3);
                Element nodeAttValue4 = doc.createElement("attvalue");
                nodeAttValue4.setAttribute("for", "4");
                nodeAttValue4.setAttribute("value", pattern.getCategory());
                nodeAttValues.appendChild(nodeAttValue4);
                Element nodeAttValue5 = doc.createElement("attvalue");
                nodeAttValue5.setAttribute("for", "5");
                nodeAttValue5.setAttribute("value", pattern.getCollection());
                nodeAttValues.appendChild(nodeAttValue5);
            }

            // Edges
            Element edgesElement = doc.createElement("edges");
            graphElement.appendChild(edgesElement);
            for (PatternLink link : this.linkList) {
                Element edgeElement = doc.createElement("edge");
                edgeElement.setAttribute("id", link.getNumber().toString());
                edgeElement.setAttribute("source", link.getSource().getId());
                edgeElement.setAttribute("target", link.getTarget().getId());
                edgeElement.setAttribute("target", link.getTarget().getId());
                edgeElement.setAttribute("type", "directed");
                edgeElement.setAttribute("weight", "1.0");
                edgeElement.setAttribute("kind", link.getType().getAttributeValue());
                edgesElement.appendChild(edgeElement);
                Element edgeAttValues = doc.createElement("attvalues");
                edgeElement.appendChild(edgeAttValues);
                // number, type
                Element edgeAttValue0 = doc.createElement("attvalue");
                edgeAttValue0.setAttribute("for", "0");
                edgeAttValue0.setAttribute("value", link.getNumber().toString());
                edgeAttValues.appendChild(edgeAttValue0);
                Element edgeAttValue1 = doc.createElement("attvalue");
                edgeAttValue1.setAttribute("for", "1");
                edgeAttValue1.setAttribute("value", link.getType().getAttributeValue());
                edgeAttValues.appendChild(edgeAttValue1);
            }
        } catch (Exception exc) {
            exc.printStackTrace();
            System.exit(1);
        }
        return doc;
    }
}
