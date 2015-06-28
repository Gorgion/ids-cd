/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import backend.entity.CdTickets;
import backend.entity.CdTrainConnection;
import backend.entity.Path;
import backend.entity.Station;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;//HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class implements ICdTariffManager. Train connection is retreaved using čd web search engine. 
 * 
 * @author Tomáš
 */
public class CdTariffManager implements ICdTariffManager
{

    private final String dataSource;
    private final String dataSourceXsd;
    private final XPath xpath;
    private Document doc;
    private final Set<String> allStations;

    /**
     * Validate XML DB according to XML Schema
     *
     */
    private void validateDataSource()
    {
        XmlValidator.validateWithXMLSchema(new File(dataSource), new File(dataSourceXsd));
    }

    public CdTariffManager() throws URISyntaxException
    {
        this.dataSourceXsd = "resources/cdAdvancedGroupTariffs.xsd";//this.getClass().getResource("resources/cdAdvancedGroupTariffs.xsd").toURI();
        this.dataSource = "resources/cdAdvancedGroupTariffs.xml";//this.getClass().getResource("resources/cdAdvancedGroupTariffs.xml").toURI();
        xpath = XPathFactory.newInstance().newXPath();

        final IPathManager pm = new PathManager();
        allStations = new HashSet<>();
        for(Station station : pm.getAllStations())
        {
            allStations.add(station.getName());
        }
    }

    /**
     * Calculate advanced group price (from xml db)
     *
     * @param ticketNode Advanced group tariff
     * @param adultsCount count of adults
     * @param childrenCounts count of children
     * @return price for group
     * @throws XPathExpressionException
     */
    private CdTickets computeAdvanceGroupPrice(Node ticketNode, int adultsCount, int childrenCounts) throws XPathExpressionException
    {
        if (adultsCount < 0 || childrenCounts < 0 || adultsCount + childrenCounts == 0)
        {
            throw new IllegalArgumentException("count of adults or children is negative or both 0");
        }

        Node ticketNameNode = (Node) xpath.evaluate("./tariffName", ticketNode, XPathConstants.NODE);
        Node maxPeopleNode = (Node) xpath.evaluate("./maxPeople", ticketNode, XPathConstants.NODE);
        Node maxAdultsNode = (Node) xpath.evaluate("./maxAdults", ticketNode, XPathConstants.NODE);
        Node maxChildrenNode = (Node) xpath.evaluate("./maxChildren", ticketNode, XPathConstants.NODE);
        Node priceNode = (Node) xpath.evaluate("./price", ticketNode, XPathConstants.NODE);

        if (ticketNameNode == null || maxAdultsNode == null || maxChildrenNode == null || maxPeopleNode == null || priceNode == null /*|| hoursNode == null*/)
        {
            return null;
        }

        String ticketName = ticketNameNode.getTextContent();
        int maxPeople = Integer.valueOf(maxPeopleNode.getTextContent());
        int maxAdults = Integer.valueOf(maxAdultsNode.getTextContent());
        int maxChildren = Integer.valueOf(maxChildrenNode.getTextContent());
        BigDecimal price = new BigDecimal(priceNode.getTextContent());

        int smallerMaxCount;
        int smallerCount;
        int higherMaxCount;
        int higherCount;

        if (maxAdults < maxChildren)
        {
            smallerCount = adultsCount;
            smallerMaxCount = maxAdults;
            higherCount = childrenCounts;
            higherMaxCount = maxChildren;
        } else
        {
            smallerCount = childrenCounts;
            smallerMaxCount = maxChildren;
            higherCount = adultsCount;
            higherMaxCount = maxAdults;
        }

        List<CdTickets.Ticket> tickets = new ArrayList<>();
        BigDecimal sum = price;
        int diff;
        int countOfTickets = 1;

        while (smallerCount + higherCount > maxPeople || smallerCount > smallerMaxCount || higherCount > higherMaxCount)
        {
            countOfTickets++;
            sum = sum.add(price);

            if (smallerCount >= smallerMaxCount)
            {
                smallerCount -= smallerMaxCount;
                diff = smallerMaxCount;
            } else
            {
                diff = smallerCount;
                smallerCount = 0;
            }

            if (higherCount >= higherMaxCount)
            {
                higherCount -= maxPeople - diff;
            } else
            {
                higherCount = 0;
            }
        }
        tickets.add(new CdTickets.Ticket(ticketName, price, countOfTickets));

        return new CdTickets(sum, tickets);
    }

    /**
     * Return train collections from CD web app for a given path
     *
     * @param fromStation from station of the path
     * @param toStation to station of the path
     * @param calendar date and time of the path
     * @return collection of train connections
     * @throws java.io.IOException due to retreaving data from web page
     */
    @Override
    public Collection<CdTrainConnection> getTrainConnections(String fromStation, String toStation, Calendar calendar) throws IOException
    {
        if (fromStation == null || fromStation.trim().isEmpty())
        {
            throw new IllegalArgumentException("'from' station is null or empty");
        }

        if (toStation == null || toStation.trim().isEmpty())
        {
            throw new IllegalArgumentException("'to' station is null or empty");
        }

        if (fromStation.equalsIgnoreCase(toStation))
        {
            throw new IllegalArgumentException("Station 'from' is the same as the 'to' station.");
        }

        if (calendar == null)
        {
            throw new IllegalArgumentException("calendar is null");
        }

//        List<String> stations = path.getStations();
//        String from = stations.get(0);
//        String to = stations.get(stations.size() - 1);
        final WebClient webClient = new WebClient();
        final HtmlPage page = webClient.getPage("http://www.cd.cz/spojeni/conn.aspx?cmd=cmdAdvanced");

        HtmlPage page1 = searchTrainConnectionWebPage(page, fromStation, toStation, calendar);

        for (int iter = 0; iter < 5; iter++)
        {
            List<CdTrainConnection> connections = trainConnectionsWebPage(page1);

            if (connections == null)
            {
                page1 = (HtmlPage) ((HtmlAnchor) page1.getFirstByXPath("//div[contains(@class, 'bckprew')]/a[2]")).click();
                continue;
            }

            webClient.closeAllWindows();

            return Collections.unmodifiableList(connections);
        }

        webClient.closeAllWindows();

        return Collections.EMPTY_LIST;
    }

    /**
     * Get several immediate train connections for a given path
     *
     * @param page search form html page
     * @param from "from" station
     * @param to "to" station
     * @return train connections from given path
     * @throws IOException
     */
    private HtmlPage searchTrainConnectionWebPage(HtmlPage page, String from, String to, Calendar calendar) throws IOException
    {
        final HtmlForm form = page.getFormByName("aspnetForm");

        final HtmlSubmitInput button = form.getInputByName("cmdSearch");
        final HtmlTextInput fromTextField = form.getInputByName("FROM_0t");
        final HtmlTextInput toTextField = form.getInputByName("TO_0t");
        final HtmlTextInput dateField = form.getInputByName("form-datum");
        final HtmlTextInput timeField = form.getInputByName("form-cas");
        final HtmlCheckBoxInput directConn = form.getInputByName("OnlyDirectConn");
        final HtmlRadioButtonInput leavingTimeRB = form.getInputByName("form-odjezd");
        final HtmlCheckBoxInput onlyCDCB = form.getInputByName("OnlyCD");
        final List<?> trTypes = form.getByXPath("//input[@name = 'TrType']");

        //GregorianCalendar today = new GregorianCalendar(TimeZone.getTimeZone("CET"), new Locale("cs", "CZ"));
        DateFormat todayFormat = DateFormat.getDateInstance(DateFormat.DEFAULT, new Locale("cs", "CZ"));
        String date = todayFormat.format(calendar.getTime());
        String dayOfWeek = new SimpleDateFormat("EE").format(calendar.getTime());
        String time = DateFormat.getTimeInstance(DateFormat.DEFAULT, new Locale("cs", "CZ")).format(calendar.getTime());

        fromTextField.setText(from);
        toTextField.setText(to);
        dateField.setValueAttribute(date + " " + dayOfWeek);
        timeField.setValueAttribute(time);
        directConn.setChecked(false);
        leavingTimeRB.setChecked(true);
        onlyCDCB.setChecked(true);

        for (Object input : trTypes)
        {
            ((HtmlCheckBoxInput) input).setChecked(false);

            if ("152".equalsIgnoreCase(((HtmlCheckBoxInput) input).getValueAttribute()))
            {
                ((HtmlCheckBoxInput) input).setChecked(true);
            }

            if ("153".equalsIgnoreCase(((HtmlCheckBoxInput) input).getValueAttribute()))
            {
                ((HtmlCheckBoxInput) input).setChecked(true);
            }
            
            if ("154".equalsIgnoreCase(((HtmlCheckBoxInput) input).getValueAttribute()))
            {
                ((HtmlCheckBoxInput) input).setChecked(true);
            }
        }

        return button.click();
    }

    /**
     * Get list of CdPrices from several train links
     *
     * @param page html page with several train links
     * @return list of CdPrices
     * @throws IOException
     */
    private List<CdTrainConnection> trainConnectionsWebPage(HtmlPage page) throws IOException
    {
        List<CdTrainConnection> trainPrices = new ArrayList<>();

        final HtmlForm form = page.getFormByName("aspnetForm");

        final List<?> connTbls = form.getByXPath("//table[contains(@class, 'conntbl')]");

        for (Object table : connTbls)
        {
            List<?> trs = ((HtmlTable) table).getByXPath("./tbody/tr");
            BigDecimal priceValue = getBasePrice((HtmlTable) table);

            List<CdTrainConnection.TrainStation> stations = getStations(trs);

            if (stations.isEmpty())
            {
                continue;
            }

            if (priceValue != null)
            {
                CdTrainConnection connection = new CdTrainConnection();
                connection.setValue(priceValue);
                connection.setStations(stations);
                //connection.setLinkId(linkId);

                //Time depTime = Time.valueOf(((HtmlTableCell) (((HtmlTable) table).getFirstByXPath("./tbody/tr[1]/td[4]"))).getTextContent() + ":00");
                //Time arrTime = Time.valueOf(((HtmlTableCell) (((HtmlTable) table).getFirstByXPath("./tbody/tr[2]/td[3]"))).getTextContent() + ":00");
                //connection.setDepartueTime(depTime);
                //connection.setArrivalTime(arrTime);
                //connection.setTrainId(((HtmlAnchor) (((HtmlTable) table).getFirstByXPath("./tbody/tr[1]/td[6]/a"))).getTextContent());
                trainPrices.add(connection);
            }
        }

        if (trainPrices.isEmpty())
        {
            return Collections.EMPTY_LIST;
        } else
        {
            return trainPrices;
        }
    }

    /**
     * Get list of train stations for one path
     *
     * @param trs List of unparsed stations with spec. information
     * @return list of strain stations
     * @throws IOException
     */
    private List<CdTrainConnection.TrainStation> getStations(List<?> trs) throws IOException
    {
        List<CdTrainConnection.TrainStation> stations = new ArrayList<>();
        int index = trs.size() - 2;

        for (Object tr : trs)
        {
            if (index == 0)
            {
                break;
            }
            index--;

            HtmlAnchor link = (HtmlAnchor) ((HtmlTableRow) tr).getFirstByXPath("./td[6]/a");
            
            String linkId = "";
            if (link != null)
            {
                if(link.getTextContent().toLowerCase().contains("bus"))
                {
                    linkId = "-";
                } else {
                    linkId = getLinkId((HtmlPage) link.click());
                }
            } else 
            {
                index = 0;
            }
            
            if (linkId == null)
            {
                return Collections.EMPTY_LIST;
            }

            CdTrainConnection.TrainStation stat = new CdTrainConnection.TrainStation();
            HtmlTableCell timeCell = (HtmlTableCell) (((HtmlTableRow) tr).getFirstByXPath("./td[4]"));

            Time depTime = null;
            Time arrTime = null;

            if (timeCell != null && timeCell.getTextContent().contains(":"))
            {
                depTime = Time.valueOf(timeCell.getTextContent() + ":00");
            }

            timeCell = (HtmlTableCell) (((HtmlTableRow) tr).getFirstByXPath("./td[3]"));
            if (timeCell != null && timeCell.getTextContent().contains(":"))
            {
                arrTime = Time.valueOf(timeCell.getTextContent() + ":00");
            }

            String trainId = null;
            HtmlAnchor train = (HtmlAnchor) ((HtmlTableRow) tr).getFirstByXPath("./td[6]/a");
            if (train != null)
            {
                trainId = train.getTextContent();
            }

            stat.setArrivalTime(arrTime);
            stat.setDepartueTime(depTime);
            stat.setLinkId(linkId);
            stat.setTrainId(trainId);

            HtmlTableCell nameCell = (HtmlTableCell) ((HtmlTableRow) tr).getFirstByXPath("./td[2]");
            String name = null;

            if (nameCell != null)
            {
                name = nameCell.getTextContent().trim();
            }

            if (!allStations.contains(name))
            {
                return Collections.EMPTY_LIST;
            }

            stat.setName(name);

            stations.add(stat);
        }

        return stations;
    }

    /**
     * Return link id from html page
     *
     * @param page page with link and train information
     * @return link id
     */
    private String getLinkId(HtmlPage page)
    {
        HtmlListItem legend = page.getFirstByXPath("//ul[@class = 'legend']/li[contains(./text(), '[IDS JMK]')]");

        if (legend == null)
        {
            return null;
        }

        String link = legend.getTextContent();

        if (link.contains("linka S") || link.contains("linka R"))
        {
            return link.split(" ", 3)[1];
        }

        return null;
    }

    /**
     * Get base price (1 person) for given train and link
     *
     * @param table HtmlTable with information of one link
     * @return BigDecimal price for one person
     */
    private BigDecimal getBasePrice(HtmlTable table)
    {
        HtmlBold price = (HtmlBold) table.getFirstByXPath("./tbody/tr/td[contains(@class, 'price')]/b[2]");
        
        if(price == null)
        {
            return null;
        }
        
        String strPrice = price.getTextContent();
        
        strPrice = strPrice.split(" ", 2)[0];

        if (strPrice != null)
        {
            return new BigDecimal(strPrice);
        } else
        {
            return null;
        }
    }

    /**
     * Calculate group price for 1 group for cd tarif
     *
     * @param basePrice base price for 1 person
     * @param personCount count of person in the group (1-30 is allowed)
     * @return BigDecimal price for one group
     */
    private BigDecimal computeGroupCdPrice(BigDecimal basePrice, int personCount)
    {
        if (basePrice == null)
        {
            throw new IllegalArgumentException("base cd price is null");
        }

        if (personCount < 0 || personCount > 30)
        {
            throw new IllegalArgumentException("count of person must be in range 0-30");
        }

        if (personCount == 0)
        {
            return BigDecimal.ZERO;
        }

        if (personCount == 1)
        {
            return basePrice;
        }

        BigDecimal groupPrice = basePrice;

        if (personCount >= 2)
        {
            groupPrice = groupPrice.add(basePrice.multiply(BigDecimal.valueOf(0.75))).setScale(0, RoundingMode.HALF_EVEN);
        }

        if (personCount >= 3)
        {
            for (int i = 3; i <= personCount; i++)
            {
                groupPrice = groupPrice.add(basePrice.multiply(BigDecimal.valueOf(0.5))).setScale(0, RoundingMode.HALF_EVEN);
            }
        }

        return groupPrice;
    }

    /**
     * Compute price for mixed group of children and addults with age emphasis
     *
     * @param basicCdPrice basic price for one person
     * @param adultsCount count of adult people
     * @param childrenCount count of children
     * @return CdTickets for the path
     */
    @Override
    public CdTickets getMixedGroupPrice(BigDecimal basicCdPrice, int adultsCount, int childrenCount)
    {
        if (basicCdPrice == null)
        {
            throw new IllegalArgumentException("price is null");
        }

        if (basicCdPrice.signum() == -1)
        {
            throw new IllegalArgumentException("price negative");
        }

        if (adultsCount < 0)
        {
            throw new IllegalArgumentException("count of adults is negative");
        }

        if (childrenCount < 0)
        {
            throw new IllegalArgumentException("count of children is negative");
        }

        if (adultsCount + childrenCount == 0)
        {
            throw new IllegalArgumentException("count of children and adults is 0");
        }

        CdTickets totalTicket = adultsCount < 1 ? null : getBasicGroupPrice(basicCdPrice, adultsCount);
        BigDecimal childrenPrice = basicCdPrice.divide(BigDecimal.valueOf(2), RoundingMode.HALF_EVEN);

        if (totalTicket == null)
        {
            totalTicket = new CdTickets(BigDecimal.ZERO, new ArrayList<CdTickets.Ticket>());
        }

        totalTicket.setTotalGroupPrice(totalTicket.getTotalGroupPrice().add(childrenPrice.multiply(BigDecimal.valueOf(childrenCount))));
        totalTicket.getTickets().add(new CdTickets.Ticket("Zlevněná jízdenka", childrenPrice, childrenCount));

        if (totalTicket.getTotalGroupPrice() == BigDecimal.ZERO)
        {
            return null;
        }

        return totalTicket;
    }

    /**
     * Calculate basic group cd price for group(s) of people.
     *
     * @param basePrice base price of cd tarif for 1 person
     * @param personCount count of person in the group (may be greater then 30)
     * @return CdTickets for the path
     */
    @Override
    public CdTickets getBasicGroupPrice(BigDecimal basePrice, int personCount)
    {
        if (personCount <= 0)
        {
            throw new IllegalArgumentException("count of person is <= 0");//return null;//BigDecimal.ZERO;
        }

        if (basePrice == null || basePrice.signum() == -1)
        {
            throw new IllegalArgumentException("price is null or negative");//return null;//BigDecimal.ZERO;
        }

        CdTickets tickets;// = new CdTickets();
        BigDecimal totalCdPrice;
        List<CdTickets.Ticket> ticket = new ArrayList<>();
        int ticketCount = 1;

        if (personCount > 30)
        {
            int divPersonCount = personCount / 30;
            ticketCount = ((personCount - 1) / 30) + 1;

            totalCdPrice = computeGroupCdPrice(basePrice, personCount % 30);
            totalCdPrice = totalCdPrice.add(computeGroupCdPrice(basePrice, 30).multiply(BigDecimal.valueOf(divPersonCount)));
        } else
        {
            totalCdPrice = computeGroupCdPrice(basePrice, personCount);
        }

        String desc = personCount == 1 ? "Základní jízdenka" : "Skupinová jízdenka";
        ticket.add(new CdTickets.Ticket(desc, basePrice, ticketCount));
        tickets = new CdTickets(totalCdPrice, ticket);

        return tickets;//totalCdPrice;
    }

    /**
     * Get advanced type of group ticket. These tickets are stored in xml db.
     *
     * @param adultsCount count of adults
     * @param childrenCounts count of children
     * @param weekendOnly ticket is allowed only on weekends
     * @param hours time of the path in hours
     * @return CdTickets for the path with the best params (e.g. lowest price)
     */
    @Override
    public CdTickets getAdvancedGroupTariffs(int adultsCount, int childrenCounts, boolean weekendOnly, int hours)
    {
        CdTickets bestTicket = null;

        if (adultsCount < 0 || childrenCounts < 0 || adultsCount + childrenCounts == 0)
        {
            throw new IllegalArgumentException("count of adults or children is negative or both 0");
        }

        if (hours < 0)
        {
            throw new IllegalArgumentException("hours must be greater than or equals 0");
        }

        validateDataSource();
//        if (!validateDataSource())
//        {
//            return null;
//        }
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        try
        {
            builder = factory.newDocumentBuilder();
            doc = builder.parse(new File(dataSource));

            String xpathGroupTicketsWeekendLimit = "//groupTariff[./@weekendOnly='" + weekendOnly + "'][./hours >='" + hours + "']";
            NodeList groupTickets = (NodeList) xpath.evaluate(xpathGroupTicketsWeekendLimit, doc, XPathConstants.NODESET);

            if (groupTickets == null || groupTickets.getLength() == 0)
            {
                return null;
            }

            CdTickets tmpTicket;
            for (int i = 0; i < groupTickets.getLength(); i++)
            {
                tmpTicket = computeAdvanceGroupPrice(groupTickets.item(i), adultsCount, childrenCounts);
                if (tmpTicket == null)
                {
                    continue;
                }

                if (bestTicket == null)
                {
                    bestTicket = tmpTicket;
                } else if (bestTicket.getTotalGroupPrice().compareTo(tmpTicket.getTotalGroupPrice()) == 1)
                {
                    bestTicket = tmpTicket;
                    //bestTicket.setWeekendOnly(weekendOnly);
                }
            }
        } catch (XPathExpressionException | SAXException | IOException | ParserConfigurationException ex)
        {
            return null;
        }

        return bestTicket;
    }

//    public static void main(String[] args) throws URISyntaxException
//    {
//        CdTariffManager mg = new CdTariffManager();
//        //try
//        {
////            List<String> stations = new ArrayList<String>();
////            //stations.add("satov");//"Brno hl.n.");
////            //stations.add("cebin");//"Břeclav");
////            stations.add("Brno hl.n.");
////            stations.add("Břeclav");
//
//            Calendar date = new GregorianCalendar();
//
//            Collection<CdTrainConnection> col = mg.getTrainConnections("", "", date);
//
//            for (CdTrainConnection item : col)
//            {
//                for(int i = 0; i < item.getStations().size() ; i++)
//                {
//                    System.out.println(item.getStations().get(i).getName() + ">" + item.getStations().get(i).getArrivalTime() + "<" + item.getStations().get(i).getDepartueTime() + ">ID>" + item.getStations().get(i).getTrainId());
//                }//System.out.println(item.getLinkId());
//                System.out.println(item.getValue());
//                //System.out.println(item.getDepartueTime());
//                //System.out.println(item.getArrivalTime());
//                System.out.println("");
//            }     
//        }
//    }
}
