/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import backend.entity.IdsPrice;
import backend.entity.OptimalGroup;
import backend.entity.OptimalTicket;
import backend.entity.Path;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class implements IIdsTariffManager.
 *
 * @author ids-cd team
 */
public class IdsTariffManager implements IIdsTariffManager {

    private String dataSource;
    private final String dataSourceXsd;
    private final XPath xpath;
    private Document doc;

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

    public IdsTariffManager() throws URISyntaxException {
        this.dataSource = "resources/tariffIDS.xml";//this.getClass().getResource("resources/tariffIDS.xml").toURI();
        this.dataSourceXsd = "resources/tariffIDS.xsd";//this.getClass().getResource("resources/tariffIDS.xsd").toURI();
        xpath = XPathFactory.newInstance().newXPath();
    }

    @Override
    public OptimalTicket getPrice(List<Path> paths, boolean weekend, int countAdults, int countChildren) throws ServiceException, ValidationException, IllegalEntityException {

        if (paths == null) {
            throw new IllegalArgumentException();
        }

        if (paths.isEmpty() || countAdults < 0 || countChildren < 0 || (countAdults == 0 && countChildren == 0)) {
            throw new IllegalEntityException();
        }

        Path path = new Path();
        int totalTime = 0;
        Set<Integer> zones = new HashSet<>();
        Set<String> stations = new HashSet<>();

        for (Path p : paths) {
            if (p.isBrno() && !path.isBrno()) {
                path.setBrno(true);
            }
            stations.addAll(p.getStations());
            totalTime += p.getTotalMinutes();
            zones.addAll(p.getZones());
        }

        path.setTotalMinutes(totalTime);
        path.setZones(new ArrayList<>(zones));
        path.setStations(new ArrayList<>(stations));
        List<IdsPrice> ticketsBase = getOptimalBase(path);

        String info = "Optimalnu cenu pre skupinu osob (" + countAdults
                + " dospely, " + countChildren + " deti) je mozne dosiahnut:" + System.lineSeparator();

        if (!weekend) {
            int tmpPrice = ticketsBase.get(0).getValue();
            int priceBase = countAdults * tmpPrice + countChildren * (tmpPrice / 2);
            info += getBaseInfo(ticketsBase);
            return new OptimalTicket(info, new BigDecimal(priceBase));
        }

        List<IdsPrice> ticketsGroup = getPrices(path, true);
        OptimalGroup optimal = getOptimalGroup(ticketsGroup, ticketsBase.get(0), countAdults, countChildren);

        info += "Pocet skupinovych listkov je " + optimal.getCountGroup()
                + ", zakladnych listkov je " + optimal.getCountBase() + " a polovicnych listkov je "
                + optimal.getCountHalf() + "." + System.lineSeparator();
        info += "\t" + optimal.getPrice().toString() + System.lineSeparator();

        for (IdsPrice price : ticketsGroup) {
            if (price.getValue() == optimal.getTotalGroupPrice()) {
                info += "\t" + price.toString() + System.lineSeparator() + System.lineSeparator();
            }
        }

        if (optimal.getCountBase() > 0 || optimal.getCountHalf() > 0) {
            for (IdsPrice pr : ticketsBase) {
                if (!pr.equals(optimal.getPrice())) {
                    info += "\t" + pr.toString() + System.lineSeparator();
                }
            }
        }
        return new OptimalTicket(info, new BigDecimal(optimal.getTotalGroupPrice()));
    }

    private String getBaseInfo(List<IdsPrice> ticketsBase) {
        String info = "";
        for (int i = 0; i < ticketsBase.size(); i++) {
            info += "\t" + ticketsBase.get(i).toString() + System.lineSeparator();
        }
        return info;
    }

    /**
     * Computes the cheapest prices for specified group and group tickets are
     * preferable
     *
     * @param prices list of all possible group tickets
     * @param base price of base ticket
     * @param countAdult count adults in group
     * @param countChildren count children in group
     * @return optimal group prices
     */
    public OptimalGroup getOptimalGroup(List<IdsPrice> prices, IdsPrice base, int countAdult, int countChildren) {
        OptimalGroup optimal = new OptimalGroup();
        int basePrice = base.getValue();
        int halfPrice = basePrice / 2;
        optimal.setTotalGroupPrice(countAdult * basePrice + halfPrice * countChildren);
        optimal.setCountBase(countAdult);
        optimal.setCountHalf(countChildren);
        optimal.setPrice(base);
        for (IdsPrice price : prices) {
            int limAdults = price.getLimAdult();
            int limChildren = price.getLimChildren();
            if (price.getValue() <= limAdults * basePrice + limChildren * halfPrice) {
                int countGroupAdult = countAdult / limAdults;
                int countGroupChildren = countChildren / limChildren;
                int fullGroups = Math.min(countGroupAdult, countGroupChildren);
                int allGroups = Math.max(countGroupAdult + Math.min(countAdult % limAdults, 1), countGroupChildren + Math.min(countChildren % limChildren, 1));
                int remainAdult = countAdult - fullGroups * limAdults;
                int remainChildren = countChildren - fullGroups * limChildren;
                // there will be max 3 "divisions"
                int firstAdult = Math.min(remainAdult, limAdults);
                int firstChild = Math.min(remainChildren, limChildren);
                if ((allGroups > fullGroups) && price.getValue() <= basePrice * firstAdult + halfPrice * firstChild) {
                    remainAdult -= firstAdult;
                    remainChildren -= firstChild;
                    int secAdult = Math.min(remainAdult, limAdults);
                    int secChild = Math.min(remainChildren, limChildren);
                    if ((allGroups > fullGroups + 1) && price.getValue() <= basePrice * secAdult + halfPrice * secChild) {
                        int thAdult = remainAdult % limAdults;
                        int thChild = remainChildren % limChildren;
                        remainAdult -= thAdult;
                        remainChildren -= thChild;

                        if ((allGroups > fullGroups + 2) && price.getValue() <= basePrice * thAdult + halfPrice * thChild) {
                            int sum = allGroups * price.getValue();
                            if (sum < optimal.getTotalGroupPrice()) {
                                optimal.setCountGroup(allGroups);
                                optimal.setCountBase(0);
                                optimal.setCountHalf(0);
                                optimal.setPrice(price);
                                optimal.setTotalGroupPrice(sum);
                            }
                        } else {
                            if (allGroups > fullGroups + 2) {
                                allGroups--;
                            }
                            int sum = allGroups * price.getValue() + thAdult * basePrice + thChild * halfPrice;
                            if (sum < optimal.getTotalGroupPrice()) {
                                optimal.setCountGroup(allGroups);
                                optimal.setCountBase(thAdult);
                                optimal.setCountHalf(thChild);
                                optimal.setPrice(price);
                                optimal.setTotalGroupPrice(sum);
                            }
                        }
                    } else {
                        fullGroups++;
                        int sum = fullGroups * price.getValue() + remainAdult * basePrice + remainChildren * halfPrice;
                        if (sum < optimal.getTotalGroupPrice()) {
                            optimal.setCountGroup(fullGroups);
                            optimal.setCountBase(remainAdult);
                            optimal.setCountHalf(remainChildren);
                            optimal.setPrice(price);
                            optimal.setTotalGroupPrice(sum);
                        }
                    }
                } else {
                    int sum = fullGroups * price.getValue() + remainAdult * basePrice + remainChildren * halfPrice;
                    if (sum < optimal.getTotalGroupPrice()) {
                        optimal.setCountGroup(fullGroups);
                        optimal.setCountBase(remainAdult);
                        optimal.setCountHalf(remainChildren);
                        optimal.setPrice(price);
                        optimal.setTotalGroupPrice(sum);
                    }
                }
            }

        }
        return optimal;
    }

    /**
     * Returns list of prices for specified path
     *
     * @param path represents one non-transferable path in IDS JMK
     * @param group true for group tickets, false otherwise
     * @return list of suitable IdsPrices
     * @throws ServiceException when XML DB operation fails
     * @throws ValidationException when XML data source breaks validation rules
     * @throws IllegalEntityException when path breaks validation rules
     */
    public List<IdsPrice> getPrices(Path path, boolean group) throws ServiceException, ValidationException, IllegalEntityException {
        validateDataSource();
        validate(path);

        try {
            int countZones = path.getZones().size();
            String restriction = (path.isBrno()) ? "brno" : "no-brno";
            int countStations = path.getStations().size();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(dataSource));

            String weekendGroup = "";
            if (group) {
                weekendGroup = "weekendGroup/";
            }

            String xpathTicketsAccordingTime = ".//" + weekendGroup
                    + "ticket[(./time[./@restriction=\'" + restriction + "\' or not(./@restriction)] >= "
                    + path.getTotalMinutes() + " and (./except!=\'" + restriction
                    + "\' or not(./except))) and (not(./stations) or (./stations >=" + countStations + "))]/@idx";

            NodeList idxAccordingTime = (NodeList) xpath.evaluate(xpathTicketsAccordingTime, doc, XPathConstants.NODESET);

            List<IdsPrice> tickets = new ArrayList<>();

            for (int i = 0; i < idxAccordingTime.getLength(); i++) {

                int idx = Integer.parseInt(idxAccordingTime.item(i).getTextContent());
                String xpathZone = getSpecialXPath(idx, "zone");
                String zoneString = xpath.evaluate(xpathZone, doc);
                int zone;
                if (zoneString.equals("unlimited")) {
                    zone = Integer.MAX_VALUE;
                } else {
                    zone = Integer.parseInt(zoneString);
                }

                if (zone >= countZones) {
                    String xpathIn = ".//ticket[@idx=\'" + idx + "\']/in";
                    NodeList inNodes = (NodeList) xpath.evaluate(xpathIn, doc, XPathConstants.NODESET);
                    List<Integer> zones = new ArrayList(path.getZones());
                    int j = 0;
                    for (; j < inNodes.getLength(); j++) {
                        int num = Integer.parseInt(inNodes.item(j).getTextContent());
                        zones.remove(new Integer(num));
                    }
                    if (j == 0 || zones.isEmpty()) {
                        String xpathPrice = getSpecialXPath(idx, "price");
                        int price = Integer.parseInt(xpath.evaluate(xpathPrice, doc));
                        IdsPrice idsPrice = createIdsPrice(idx, price, restriction);
                        if (group) {
                            idsPrice.setType(IdsPrice.TicketType.WEEKEND_GROUP);
                        } else {
                            idsPrice.setType(IdsPrice.TicketType.BASIC);
                        }
                        tickets.add(idsPrice);
                    }

                }
            }
            return tickets;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new ServiceException("Error when parsing XML with tariff info!", ex);
        } catch (XPathExpressionException ex) {
            throw new ServiceException("Error when getting data from XML DB!", ex);
        } catch (NumberFormatException ex) {
            throw new ServiceException("Error when getting integer data from XML DB!", ex);
        }
    }

    /**
     * Returns the cheapest base tickets
     *
     * @param path represents one non-transferable path in IDS JMK
     * @return list of the cheapest base tickets
     * @throws ServiceException when XML DB operation fails
     * @throws ValidationException when XML data source breaks validation rules
     * @throws IllegalEntityException when path breaks validation rules
     */
    public List<IdsPrice> getOptimalBase(Path path) throws ServiceException, ValidationException, IllegalEntityException {
        List<IdsPrice> prices = getPrices(path, false);
        int cheapestIdx = 0;
        for (int i = 0; i < prices.size(); i++) {
            if (prices.get(i).getValue() < prices.get(cheapestIdx).getValue()) {
                cheapestIdx = i;
            }
        }
        List<IdsPrice> optimal = new ArrayList<>();
        for (IdsPrice price : prices) {
            if (price.getValue() == prices.get(cheapestIdx).getValue()) {
                optimal.add(price);
            }
        }
        return optimal;
    }

    /**
     * Returns special XPath expression
     *
     * @param idx idx attribute of ticket
     * @param element ticket element
     * @return special XPath expression
     */
    private String getSpecialXPath(int idx, String element) {
        return ".//ticket[@idx=" + idx + "]/" + element;
    }

    /**
     * Creates IdsPrice entity
     *
     * @param idx idx attribute of ticket
     * @param minPrice ticket price
     * @param restriction restriction attribute of time
     * @return IdsPrice entity
     * @throws XPathExpressionException represents an error in an XPath
     * expression
     * @throws NumberFormatException converted string does not have the
     * appropriate format
     */
    private IdsPrice createIdsPrice(int idx, int price, String restriction) throws XPathExpressionException, NumberFormatException {
        IdsPrice idsPrice = new IdsPrice();
        idsPrice.setValue(price);
        String xpathZone = getSpecialXPath(idx, "zone");
        String zoneString = xpath.evaluate(xpathZone, doc);
        if (zoneString.equals("unlimited")) {
            idsPrice.setZoneCountLimit(Integer.MAX_VALUE);
        } else {
            idsPrice.setZoneCountLimit(Integer.parseInt(zoneString));
        }
        String xpathTime = ".//ticket[@idx=" + idx + "]/time[./@restriction=\'" + restriction + "\' or not(./@restriction)]";
        String pathTime = xpath.evaluate(xpathTime, doc);
        idsPrice.setTimeLimit(Integer.parseInt(pathTime));

        String xpathExcept = getSpecialXPath(idx, "except");
        String except = xpath.evaluate(xpathExcept, doc);
        if (!except.isEmpty()) {
            idsPrice.setExcept(except);
        } else {
            String xpathIn = ".//ticket[@idx=\'" + idx + "\']/in";
            NodeList inNodes = (NodeList) xpath.evaluate(xpathIn, doc, XPathConstants.NODESET);
            List<Integer> zones = new ArrayList();
            for (int j = 0; j < inNodes.getLength(); j++) {
                int num = Integer.parseInt(inNodes.item(j).getTextContent());
                zones.add(num);
            }
            idsPrice.setInZones(zones);
        }

        String xpathStationLimit = getSpecialXPath(idx, "/stations");
        String stationLimit = xpath.evaluate(xpathStationLimit, doc);

        if (!stationLimit.isEmpty()) {

            idsPrice.setStationLimit(Integer.parseInt(stationLimit));
        }

        String xpathAdultLim = getSpecialXPath(idx, "@maxAdults");
        String xpathChildrenLim = getSpecialXPath(idx, "@maxChildren");

        String adultLimStr = xpath.evaluate(xpathAdultLim, doc);
        if (!adultLimStr.isEmpty()) {
            int childrenLim = Integer.parseInt(xpath.evaluate(xpathChildrenLim, doc));
            int adultLim = Integer.parseInt(adultLimStr);
            idsPrice.setLimAdult(adultLim);
            idsPrice.setLimChildren(childrenLim);
        }
        return idsPrice;
    }

    /**
     * Validates Path entity
     *
     * @param path path entity
     * @throws IllegalEntityException when path breaks some rules
     */
    private static void validate(Path path) throws IllegalEntityException {

        if (path == null) {
            throw new IllegalEntityException("Path is null!");
        }
        if (path.getZones() == null || path.getZones().isEmpty()) {
            throw new IllegalEntityException("Zone count is null or negative!");
        }
        if (path.getTotalMinutes() == null || path.getTotalMinutes() < 0) {
            throw new IllegalEntityException("Total minutes is null or negative!");
        }

        if (path.getStations() == null || path.getStations().isEmpty()) {
            throw new IllegalEntityException("List of stations is null or empty!");
        }

        for (String str : path.getStations()) {
            if (str == null || str.isEmpty()) {
                throw new IllegalEntityException("Station is null or empty!");
            }
        }

    }
}
