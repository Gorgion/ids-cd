/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import backend.entity.Path;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Comparator;
import backend.entity.Station;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Class PathManager
 *
 * @author ids-cd team
 */
public class PathManager implements IPathManager {

    private String dataSource;
    private final String dataSourceXsd;
    private final XPath xpath;
    private Document doc;
    private final String stations;
    private final String stationsXsd;

    /**
     * Setting data source of XML DB
     *
     * @param dataSource path to XML file
     */
    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Validate XML DB according to XML Schema
     *
     * @throws ValidationException when XML file breaks validation rules
     */
    private void validateDataSource() throws ValidationException {
        XmlValidator.validateWithXMLSchema(new File(dataSource), new File(dataSourceXsd));
    }

    /**
     * Validate XML DB according to XML Schema
     *
     * @throws ValidationException when XML file breaks validation rules
     */
    private void validateStations() throws ValidationException {
        XmlValidator.validateWithXMLSchema(new File(stations), new File(stationsXsd));
    }

    public PathManager() throws URISyntaxException {
//        final ClassLoader cl = this.getClass().getClassLoader();
        
//        this.dataSource = cl.getResource("backend/resources/linksIDS.xml").toURI();
//        this.dataSourceXsd = cl.getResource("backend/resources/linksIDS.xsd").toURI();
//        this.stations = cl.getResource("backend/resources/coordinates.xml").toURI();
//        this.stationsXsd = cl.getResource("backend/resources/coordinates.xsd").toURI();
        this.dataSource = "resources/linksIDS.xml";
        this.dataSourceXsd = "resources/linksIDS.xsd";
        this.stations = "resources/coordinates.xml";
        this.stationsXsd = "resources/coordinates.xsd";

        
        xpath = XPathFactory.newInstance().newXPath();
        
    }

    /**
     * Returns names of all stations
     *
     * @return names of all stations
     * @throws ServiceException when XML DB operation fails
     * @throws ValidationException when XML file breaks validation rules
     */
    private List<String> getAllStationsNames() throws ServiceException, ValidationException {
        validateStations();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(dataSource));

            String xpathElements = ".//name";

            NodeList elements = (NodeList) xpath.evaluate(xpathElements, doc, XPathConstants.NODESET);

            List<String> elems = new ArrayList<>();

            for (int i = 0; i < elements.getLength(); i++) {
                elems.add(elements.item(i).getTextContent());
            }
            return elems;

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ServiceException("Error when parsing XML with tariff info!", ex);
        } catch (XPathExpressionException ex) {
            throw new ServiceException("Error when getting data from XML DB!", ex);
        }

    }

    @Override
    public Set<Station> getAllStations() throws ServiceException {
        XmlValidator.validateWithXMLSchema(new File(stations), new File(stationsXsd));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        Document doc;
        try {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(stations));

            String xpathElements = "//station";

            NodeList elements = (NodeList) xpath.evaluate(xpathElements, doc, XPathConstants.NODESET);

            Set<Station> setElements = new HashSet<>();

            for (int i = 0; i < elements.getLength(); i++) {
                Node name = ((Element) elements.item(i)).getElementsByTagName("name").item(0);

                Node latitudeDegree = ((Element) elements.item(i)).getElementsByTagName("latitude-degree").item(0);
                Node latitudeMinute = ((Element) elements.item(i)).getElementsByTagName("latitude-minute").item(0);
                Node latitudeSecond = ((Element) elements.item(i)).getElementsByTagName("latitude-second").item(0);

                Node longitudeDegree = ((Element) elements.item(i)).getElementsByTagName("longitude-degree").item(0);
                Node longitudeMinute = ((Element) elements.item(i)).getElementsByTagName("longitude-minute").item(0);
                Node longitudeSecond = ((Element) elements.item(i)).getElementsByTagName("longitude-second").item(0);

                Station station = new Station();
                station.setName(name.getTextContent());
                station.setLatitudeDegree(Integer.valueOf(latitudeDegree.getTextContent()));
                station.setLatitudeMinute(Integer.valueOf(latitudeMinute.getTextContent()));
                station.setLatitudeSecond(Integer.valueOf(latitudeSecond.getTextContent()));

                station.setLongitudeDegree(Integer.valueOf(longitudeDegree.getTextContent()));
                station.setLongitudeMinute(Integer.valueOf(longitudeMinute.getTextContent()));
                station.setLongitudeSecond(Integer.valueOf(longitudeSecond.getTextContent()));

                setElements.add(station);
            }
            return setElements;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ServiceException("Error when parsing XML with tariff info!", ex);
        } catch (XPathExpressionException ex) {
            throw new ServiceException("Error when getting data from XML DB!", ex);
        }
    }

    /**
     * Returns special XPath expression
     *
     * @param linkID id attribute of link
     * @param stationArg station element
     * @param lastXpathArg last element in XPath expression
     * @return special XPath expression
     */
    private String getSpecialXPath(String linkID, String stationArg, String lastXPathArg) {
        return ".//link[@id=\'" + linkID + "\']//station[" + stationArg + "]/" + lastXPathArg;
    }

    @Override
    public Path getPath(String from, String to) throws ServiceException, ValidationException, IllegalEntityException {
        if (from == null || to == null || from.trim().isEmpty() || to.trim().isEmpty()) {
            throw new IllegalArgumentException();
        }
        validateDataSource();
        try {

            from = findXMLStation(from);
            to = findXMLStation(to);
            if (from == null || to == null) {
                throw new IllegalEntityException("From or to station does not exists!");
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(dataSource));

            String xpathId = ".//link[.//name=\'" + from + "\'and .//name=\'" + to + "\']/@id";
            String id = xpath.evaluate(xpathId, doc);
            if (id.isEmpty()) {
                throw new IllegalEntityException("There does not exists non-transferable link in IDS JMK with stations " + from + " and " + to + "!");
            }

            String xpathFrom = ".//link[@id=\'" + id + "\']//station[./name=\'" + from + "\']/@idx";
            String xpathTo = ".//link[@id=\'" + id + "\']//station[./name=\'" + to + "\']/@idx";

            int idxFrom = Integer.parseInt(xpath.evaluate(xpathFrom, doc));
            int idxTo = Integer.parseInt(xpath.evaluate(xpathTo, doc));

            int idxMin = Math.min(idxFrom, idxTo);
            int idxMax = Math.max(idxFrom, idxTo);

            String xpathNames = null;
            String xpathZones = null;

            String xpathVariant = ".//link[@id=\'" + id + "\']/variant";
            String variant = xpath.evaluate(xpathVariant, doc);
            if (!variant.isEmpty()) {
                String xpathLastStation = ".//link[@id=\'" + id + "\']/station[last()]/@idx";
                int idxLastStation = Integer.parseInt(xpath.evaluate(xpathLastStation, doc));
                if (idxLastStation >= idxMin && idxLastStation < idxMax) {
                    String xpathFirstStationVariant = ".//link[@id=\'" + id + "\']/variant[.//@idx=" + idxMax + "]/station[1]/@idx";
                    int idxFirstStationVariant = Integer.parseInt(xpath.evaluate(xpathFirstStationVariant, doc));
                    xpathNames = getSpecialXPath(id, "(@idx>=" + idxMin + " and @idx<=" + idxLastStation + ") or (@idx>=" + idxFirstStationVariant + " and @idx<=" + idxMax + ")", "name");
                    xpathZones = getSpecialXPath(id, "(@idx>=" + idxMin + " and @idx<=" + idxLastStation + ") or (@idx>=" + idxFirstStationVariant + " and @idx<=" + idxMax + ")", "zone");
                }
            }
            if (xpathNames == null) {
                xpathNames = getSpecialXPath(id, "@idx>=" + idxMin + " and @idx<=" + idxMax, "name");
                xpathZones = getSpecialXPath(id, "@idx>=" + idxMin + " and @idx<=" + idxMax, "zone");
            }

            NodeList names = (NodeList) xpath.evaluate(xpathNames, doc, XPathConstants.NODESET);
            NodeList zones = (NodeList) xpath.evaluate(xpathZones, doc, XPathConstants.NODESET);

            List<String> stations = new ArrayList<>();

            Set<Integer> zoneSet = new HashSet<>();
            int newZone;

            Path path = new Path();
            path.setBrno(false);

            for (int k = 0; k < names.getLength(); k++) {
                stations.add(names.item(k).getTextContent());
                newZone = Integer.parseInt(zones.item(k).getTextContent());
                zoneSet.add(newZone);
                if (!path.isBrno() && (newZone == 100 || newZone == 101)) {
                    path.setBrno(true);
                }
            }

            if (!stations.get(0).equals(from)) {
                Collections.reverse(stations);
            }

            path.setZones(new ArrayList<>(zoneSet));
            path.setStations(stations);
            return path;

        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ServiceException("Error when parsing XML with tariff info!", ex);
        } catch (XPathExpressionException ex) {
            throw new ServiceException("Error when getting data from XML DB!", ex);
        } catch (NumberFormatException ex) {
            throw new ServiceException("Error when getting integer data from XML DB!", ex);
        }
    }

    /**
     * Finds competent station to name according alphabetic characters
     *
     * @param name input string
     * @return competent station name
     */
    private String findXMLStation(String name) {
        String normStr = Normalizer.normalize(name, Normalizer.Form.NFD).replaceAll("[^A-Za-z]", "");
        normStr = normStr.toLowerCase();
        for (String s : getAllStationsNames()) {
            if (Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("[^A-Za-z]", "").toLowerCase().equals(normStr)) {
                return s;
            }
        }
        return null;
    }

    /*
     public static void main(String[] args) throws URISyntaxException
     {
     PathManager mgr = new PathManager();
     Set<Station> stat = mgr.getAllStations();
     List<Station> s = new ArrayList<>(stat);
     Collections.sort(s, new Comparator<Station>()
     {

     @Override
     public int compare(Station o1, Station o2)
     {
     return o2.getName().length() - o1.getName().length();
     }
            
     });
     Path path = mgr.getPath("Brno hl.n.","Blansko-mesto");
     List<String> stations = path.getStations();
     for (Station str : s)
     {
     System.out.println(str.getName());
     System.out.println(str.getLatitudeDegree() + ":" + str.getLatitudeMinute() + ":" + str.getLatitudeSecond());
     }
      
     }*/
}
