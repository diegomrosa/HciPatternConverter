import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.SortedSet;
import java.util.TreeSet;

public class Main {
    private static final String PATTERN_FOLDER = "patterns";
    private static final String GEXF_FOLDER = "gexf";
    private static final String XML_EXTENSION = ".xml";

    public static void main(String[] args) {
        System.out.println("Starting HCI Patterns converter!");

        String currentDirPath =  System.getProperty("user.dir");
        File currentDir = new File(currentDirPath);
        File patternsDir = new File(currentDir, PATTERN_FOLDER);
        File gexfDir = new File(currentDir, GEXF_FOLDER);

        if (!patternsDir.exists()) {
            System.err.println("Could not open folder: " + patternsDir);
            System.exit(1);
        }
        System.out.println("Current DIR is: " + currentDirPath);
        System.out.println("Patterns folder is: " + patternsDir);

        SortedSet<File> xmlFiles = getSortedFiles(patternsDir);
//        System.out.println("List of files:");
//        for (File file : xmlFiles) {
//            System.out.println("FILE: " + file.getAbsolutePath());
//        }

        PatternLanguage language = new PatternLanguage();

        language.parseXmlFiles(xmlFiles);
        Document gexfDom = language.generateGexfDom();

        File gexfFile = new File(gexfDir, "welie_pattern_language.gexf");
        try (FileOutputStream output = new FileOutputStream(gexfFile)) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(gexfDom);
            StreamResult result = new StreamResult(output);

            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static SortedSet<File> getSortedFiles(File parentDir) {
        SortedSet<File> sortedFiles = new TreeSet<File>();
        for (File file : parentDir.listFiles()) {
            if (file.getName().contains(".xml")) {
                sortedFiles.add(file);
            }
        }
        return sortedFiles;
    }
}
