package ru.bitte.lab6.server;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import ru.bitte.lab6.exceptions.*;
import ru.bitte.lab6.route.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * An object of the {@code Parser} class can be used to read objects of the {@link Route} class from an XML file
 * and return them in a list, and to write {@code Route} objects from a collection to an XML file.
 * <br>The objects in a read-from XML file must adhere to the following format:
 * <pre>
 * {@code
 * <collection>
 *      <route>
 *          <name>...</name>
 *          <coordinates>
 *              <x>...</x>
 *              <y>...</y>
 *          </coordinates>
 *          <from>
 *              <x>...</x>
 *              <y>...</y>
 *              <z>...</z>
 *              <name>...</name>
 *          </from>
 *          <to>
 *              <x>...</x>
 *              <y>...</y>
 *              <z>...</z>
 *              <name>...</name>
 *          </to>
 *      </route>
 *      ...
 *      <route>...</route>
 * </collection>
 * }
 * </pre>
 */
public class Parser {
    private final DocumentBuilder documentBuilder;
    private final Transformer transformer;

    /**
     * Returns an instance of the parser.
     * @throws ParserConfigurationException if the XML parser configuration exception was thrown
     * @throws TransformerConfigurationException if the XML transformer configuration exception was thrown
     */
    public Parser() throws ParserConfigurationException, TransformerConfigurationException {
        // initializes a document builder and a transformer for handling xml files
        documentBuilder = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
        transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "yes");
        transformer.setOutputProperty("{https://xml.apache.org/xslt}indent-amount", "4");
    }


    /**
     * Reads {@code Route} objects from an XML file and returns them in a list.
     * @param file the {@code File} object representing an XML file containing objects
     * @return an {@link ArrayList} containing parsed {@code Route} objects
     * @throws IOException if an input/output exception occurred
     * @throws SAXException if a SAX exception occurred
     * @throws ElementParsingInFileException if an element couldn't be properly parsed from a file, including logical
     * and formatting errors (see a particular exception message for more)
     */
    public ArrayList<Route> readFromFile(File file) throws IOException, SAXException,
            ElementParsingInFileException {
        // very ad hoc shitty parser
        try {
            ArrayList<Route> generatedRoutes = new ArrayList<>();
            // parse a file and make sure nothing other than route is present
            Document doc = documentBuilder.parse(file);
            Element root = doc.getDocumentElement();
            for (Node node : clearUpNodes(root.getChildNodes())) {
                if (!node.getNodeName().equals("route")) {
                    throw new ElementParsingInFileException("Unexpected element name - " + node.getNodeName());
                }
            }
            NodeList routes = root.getElementsByTagName("route");
            // process a route element
            for (int i = 0; i < routes.getLength(); i++) {
                NodeList rawNodes = routes.item(i).getChildNodes(); // get the route's nodes
                ArrayList<Node> routeNodes = clearUpNodes(rawNodes); // clear the node list from text nodes
                // clear up the route nodes from the ignored tags
                ArrayList<String> ignoredTags = new ArrayList<>(Arrays.asList("id", "creationDate", "distance"));
                routeNodes.removeIf(node -> ignoredTags.contains(node.getNodeName()));
                // define the list of allowed tags
                ArrayList<String> allowedTags = new ArrayList<>(Arrays.asList("coordinates", "from", "name", "to"));
                ArrayList<String> tagNames = getTagNames(routeNodes); // get the supplied tag names
            /* if the number of supplied tag names is less than or greater than the number of allowed ones,
             an exception is thrown */
                Collections.sort(tagNames); // allows the elements be in different order (since allowed tags are sorted)
                if (allowedTags.size() > tagNames.size()) {
                    throw new ElementParsingInFileException("Missing tag names detected in route");
                } else if (!allowedTags.equals(tagNames)) {
                    throw new ElementParsingInFileException("Duplicate or illegal tag names detected in route");
                }
                // get the fields
                String name = null;
                Coordinates coords = null;
                Location from = null, to = null;
                // allowed tag for coordinates and location objects
                ArrayList<String> allowedCoordTags = new ArrayList<>(Arrays.asList("x", "y"));
                ArrayList<String> allowedLocTags = new ArrayList<>(Arrays.asList("name", "x", "y", "z"));
                // read the inside nodes of route in a loop
                for (Node node : routeNodes) {
                    switch (node.getNodeName()) {
                        case "name" -> name = node.getTextContent().strip();
                        case "coordinates" -> {
                            // clear up nodes and confirm them
                            ArrayList<Node> coordNodes = clearUpNodes(node.getChildNodes());
                            ArrayList<String> coordTags = getTagNames(coordNodes);
                            Collections.sort(coordTags); // allows the elements be in different order
                            if (allowedCoordTags.size() > coordTags.size()) {
                                throw new ElementParsingInFileException("Missing tag names detected in coordinates");
                            } else if (!coordTags.equals(allowedCoordTags)) {
                                throw new ElementParsingInFileException("Duplicate or illegal names detected in coordinates");
                            }
                            long x = 0, y = 0;
                            // extract them
                            for (Node coordNode : coordNodes) {
                                if (coordNode.getNodeName().equalsIgnoreCase("x")) {
                                    x = Long.parseLong(coordNodes.get(0).getTextContent());
                                } else if (coordNode.getNodeName().equalsIgnoreCase("y")) {
                                    y = Long.parseLong(coordNodes.get(1).getTextContent());
                                }
                            }
                            // create a coordinates object and pass on a construction exception
                            try {
                                coords = new Coordinates(x, y);
                            } catch (ElementConstructionException e) {
                                throw new ElementParsingInFileException(e.getMessage());
                            }
                        }
                        case "from", "to" -> { // aka location
                            // clear up nodes and confirm them
                            ArrayList<Node> locNodes = clearUpNodes(node.getChildNodes());
                            ArrayList<String> locTags = getTagNames(locNodes);
                            Collections.sort(locTags); // allows the elements be in different order
                            if (allowedLocTags.size() > locTags.size()) {
                                throw new ElementParsingInFileException("Missing tag names detected in location");
                            } else if (!locTags.equals(allowedLocTags)) {
                                throw new ElementParsingInFileException("Duplicate or illegal tag names detected in location");
                            }
                            // extract them
                            long x = 0, y = 0;
                            float z = 0;
                            String locName = null;
                            for (Node locNode : locNodes) {
                                if (locNode.getNodeName().equalsIgnoreCase("x")) {
                                    x = Long.parseLong(locNodes.get(0).getTextContent());
                                } else if (locNode.getNodeName().equalsIgnoreCase("y")) {
                                    y = Long.parseLong(locNodes.get(1).getTextContent());
                                } else if (locNode.getNodeName().equalsIgnoreCase("z")) {
                                    z = Float.parseFloat(locNode.getTextContent());
                                } else if (locNode.getNodeName().equals("name")) {
                                    locName = locNode.getTextContent().strip();
                                }
                            }
                            // assign the new location object to either "from" or "to" field depending on the tag name
                            if (node.getNodeName().equals("from")) {
                                assert !(locName == null);
                                from = new Location(x, y, z, locName);
                            } else if (node.getNodeName().equals("to")) {
                                assert !(locName == null);
                                to = new Location(x, y, z, locName);
                            }
                        }
                        case "id", "creationDate", "distance" -> {
                            // skip these
                        }
                        default -> throw new ElementParsingInFileException("Unexpected value: " + node.getNodeName());
                    }
                }
                // add a new element and pass on a ElementConstructionException to a ElementParsingInFileException
                try {
                    Route newRoute = new Route(name, coords, from, to);
                    generatedRoutes.add(newRoute);
                } catch (ElementConstructionException e) {
                    throw new ElementParsingInFileException(e.getMessage());
                }
            }
            assert generatedRoutes.size() == routes.getLength();
            return generatedRoutes;
        } catch (NumberFormatException e) { // I know this is bad but at least it's passed on to another exception
            throw new ElementParsingInFileException("Error while parsing the coordinates/location number types " +
                    "(see documentation on required data types).");
        }
    }

    /**
     * Writes {@code Route} objects from a collection to a new XML file in the above defined format.
     * @param collection the collection from which to gather the {@code Route} objects
     * @param file the {@code File} object representation of a file to which write the objects in
     * @throws TransformerException if an XML transformer exception occurred
     */
    public void writeToFile(Collection<Route> collection, File file) throws TransformerException {
        // create a new document tree with the element "collection" as the root
        Document document = documentBuilder.newDocument();
        Element root = document.createElement("collection");
        document.appendChild(root);
        for (Route r : collection) {
            // create a new route element to be filled with other elements
            Element route = document.createElement("route");
            // set id
            Element id = document.createElement("id");
            id.setTextContent(String.valueOf(r.getId()));
            // set name
            Element name = document.createElement("name");
            name.setTextContent(r.getName());
            route.appendChild(name);
            // set coordinates
            Element xCoords = document.createElement("x");
            xCoords.setTextContent(String.valueOf(r.getCoordinates().getX()));
            Element yCoords = document.createElement("y");
            yCoords.setTextContent(String.valueOf(r.getCoordinates().getY()));
            Element coords = document.createElement("coordinates");
            coords.appendChild(xCoords);
            coords.appendChild(yCoords);
            route.appendChild(coords);
            // set creation date
            Element creationDate = document.createElement("creationDate");
            creationDate.setTextContent(r.getFormattedDate());
            route.appendChild(creationDate);
            Element xFrom = document.createElement("x");
            xFrom.setTextContent(String.valueOf(r.getFrom().getX()));
            Element yFrom = document.createElement("y");
            yFrom.setTextContent(String.valueOf(r.getFrom().getY()));
            Element zFrom = document.createElement("z");
            zFrom.setTextContent(String.valueOf(r.getFrom().getZ()));
            Element nameFrom = document.createElement("name");
            nameFrom.setTextContent(r.getFrom().getName());
            Element from = document.createElement("from");
            from.appendChild(xFrom);
            from.appendChild(yFrom);
            from.appendChild(zFrom);
            from.appendChild(nameFrom);
            route.appendChild(from);
            // set to
            Element xTo = document.createElement("x");
            xTo.setTextContent(String.valueOf(r.getTo().getX()));
            Element yTo = document.createElement("y");
            yTo.setTextContent(String.valueOf(r.getTo().getY()));
            Element zTo = document.createElement("z");
            zTo.setTextContent(String.valueOf(r.getTo().getZ()));
            Element nameTo = document.createElement("name");
            nameTo.setTextContent(r.getTo().getName());
            Element to = document.createElement("to");
            to.appendChild(xTo);
            to.appendChild(yTo);
            to.appendChild(zTo);
            to.appendChild(nameTo);
            route.appendChild(to);
            // set distance
            Element distance = document.createElement("distance");
            distance.setTextContent(String.valueOf(r.getDistance()));
            route.appendChild(distance);
            // append a new route element
            root.appendChild(route);
        }
        // transform an xml tree to an xml file
        DOMSource source = new DOMSource(root);
        StreamResult result = new StreamResult(file);
        transformer.transform(source, result);
    }

    // cleans up a node's elements from unwanted text nodes and makes sure they're empty
    private ArrayList<Node> clearUpNodes(NodeList nodes) throws ElementParsingInFileException {
        // get text nodes and element nodes in separate lists
        ArrayList<Node> textNodes = new ArrayList<>();
        ArrayList<Node> elementNodes = new ArrayList<>();
        for (int j = 0; j < nodes.getLength(); j++) {
            if (nodes.item(j).getNodeType() == Node.TEXT_NODE) {
                textNodes.add(nodes.item(j));
            } else if (nodes.item(j).getNodeType() == Node.ELEMENT_NODE) {
                elementNodes.add(nodes.item(j));
            }
        }
        // make sure the text nodes are empty
        for (Node node : textNodes) {
            if (!(node.getTextContent().strip().equals(""))) {
                throw new ElementParsingInFileException("Unknown text fields in the route element detected");
            }
        }
        return elementNodes;
    }

    // returns tag names of a nodes list
    private ArrayList<String> getTagNames(ArrayList<Node> nodes) {
        ArrayList<String> tagNames = new ArrayList<>();
        for (Node node : nodes) {
            tagNames.add(node.getNodeName().strip().toLowerCase());
        }
        return tagNames;
    }
}
