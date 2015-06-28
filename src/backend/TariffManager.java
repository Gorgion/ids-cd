/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import backend.entity.CdTickets;
import backend.entity.CdTrainConnection;
import backend.entity.OptimalTicket;
import backend.entity.Path;
import backend.entity.Prices;
import backend.entity.Station;
import backend.entity.Train;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Tomáš
 */
public class TariffManager implements ITariffManager
{

    private static final Set<Calendar> notWorkingDay = new HashSet<>();

    static
    {
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.JANUARY, 1));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.MAY, 1));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.MAY, 8));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.JULY, 5));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.JULY, 6));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.SEPTEMBER, 28));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.OCTOBER, 28));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.NOVEMBER, 17));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.DECEMBER, 24));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.DECEMBER, 25));
        notWorkingDay.add(new GregorianCalendar(GregorianCalendar.getInstance().get(Calendar.YEAR), Calendar.DECEMBER, 26));
    }

    private final ICdTariffManager cdManager;
    private final IIdsTariffManager idsManager;
    private final IPathManager pathManager;

    public TariffManager() throws URISyntaxException
    {
        this.pathManager = new PathManager();
        this.idsManager = new IdsTariffManager();
        this.cdManager = new CdTariffManager();
    }

    /**
     * Compute tarif for CD and for IDS JMK for a given path and people.
     *
     * @param from starting station of path
     * @param to ending station of path
     * @param calendar date and time of the path
     * @param adultsCount count of adult people
     * @param childrenCount count of children in the group
     * @return Map<Train, Prices> - Train contains path information, Prices
     * contains ids and cd price information
     */
    @Override
    public Map<Train, Prices> computePrices(String from, String to, Calendar calendar, int adultsCount, int childrenCount)
    {
        if (from == null || from.trim().isEmpty())
        {
            throw new IllegalArgumentException("'from' station is null or empty");
        }

        if (to == null || to.trim().isEmpty())
        {
            throw new IllegalArgumentException("'to' station is null or empty");
        }

        if (calendar == null)
        {
            throw new IllegalArgumentException("calendar is null");
        }

        if (adultsCount < 0)
        {
            throw new IllegalArgumentException("count of adults must be grater then or equals to 0");
        }

        if (childrenCount < 0)
        {
            throw new IllegalArgumentException("count of children must be grater then or equals to 0");
        }

        if (adultsCount + childrenCount == 0)
        {
            throw new IllegalArgumentException("count of adults and children is equals to 0");
        }

        if (from.equalsIgnoreCase(to))
        {
            throw new IllegalArgumentException("Station 'from' is the same as the 'to' station.");
        }

        boolean isWorkingDay = isWorkingDay(calendar);

        Map<Train, Prices> allPrices = new HashMap<>();
        Set<String> allStations = new HashSet<>();
        allStations = new HashSet<>();
        for(Station station : pathManager.getAllStations())
        {
            allStations.add(station.getName());
        }

        if (!allStations.contains(from))
        {
            throw new IllegalArgumentException("station: '" + from + "' dos not exist in both IDS JKM and ČD");
        }

        if (!allStations.contains(to))
        {
            throw new IllegalArgumentException("station: '" + to + "' dos not exist in both IDS JKM and ČD");
        }

        Collection<CdTrainConnection> trainConnections = getCdTrainConnections(from, to, calendar);

        for (CdTrainConnection trainConnection : trainConnections)
        {
            List<Path> paths = trainConnectionToPaths(trainConnection);//new ArrayList<>(trainConnections.size());

            if (paths == null || paths.isEmpty())
            {
                continue;//throw new ServiceException("cannot retrieve train connections");
            }

            List<CdTickets> cdTickets = new ArrayList<>();

            CdTickets basicGroupCdPrice = cdManager.getBasicGroupPrice(trainConnection.getValue(), adultsCount + childrenCount);
            if (basicGroupCdPrice != null)
            {
                cdTickets.add(basicGroupCdPrice);
            }

            CdTickets advancedGroupCdTicket = cdManager.getAdvancedGroupTariffs(adultsCount, childrenCount, !isWorkingDay, adultsCount);//computeWeekendGroupCdPrice(adultsCount, childrenCount);
            if (advancedGroupCdTicket != null)
            {
                cdTickets.add(advancedGroupCdTicket);
            }

            CdTickets mixedGroupCdPrice = cdManager.getMixedGroupPrice(trainConnection.getValue(), adultsCount, childrenCount);
            if (mixedGroupCdPrice != null && childrenCount > 0)
            {
                cdTickets.add(mixedGroupCdPrice);
            }

            OptimalTicket idsTicket = getIdsPrice(paths, !isWorkingDay, adultsCount, childrenCount);
            CdTickets cdTicket = getBestTicket(cdTickets);

            Prices price = new Prices(cdTicket, idsTicket);

            Train train = newTrain(trainConnection);

            allPrices.put(train, price);
        }

        if (allPrices.isEmpty())
        {
            return Collections.EMPTY_MAP;
        } else
        {
            return allPrices;
        }

    }

    /**
     * Get best ticket according the criteria (e.g. price)
     *
     * @param tickets list of cd tickets
     * @return best tictet
     */
    private CdTickets getBestTicket(List<CdTickets> tickets)
    {
        CdTickets bestTisket = null;

        for (CdTickets ticket : tickets)
        {
            if (bestTisket == null)
            {
                bestTisket = ticket;
            } else if (bestTisket.getTotalGroupPrice().compareTo(ticket.getTotalGroupPrice()) == 1)
            {
                bestTisket = ticket;
            }
        }

        return bestTisket;
    }

    /**
     * Get Path from PathManager
     *
     * @param from "from" station
     * @param to "to" station
     * @return Path from db
     */
    private Path getPath(String from, String to)
    {
        try
        {
            return pathManager.getPath(from, to);
        } catch (IllegalEntityException | ValidationException ex)//(ParserConfigurationException | SAXException | XPathExpressionException | IOException ex)
        {
            throw new ServiceException("Error during retreaving path from xml db.", ex);
        }
    }

    /**
     * Get collection of prices and links from cd web app
     *
     * @param from from station
     * @param to to station
     * @param calendar date and time of the path
     * @return Collection of train connection
     */
    private Collection<CdTrainConnection> getCdTrainConnections(String from, String to, Calendar calendar)
    {
        try
        {
            return cdManager.getTrainConnections(from, to, calendar);
        } catch (IOException ex)
        {
            throw new ServiceException("Error during retreaving prices from cd web page.", ex);
        }
    }

    /**
     * Get optimal ticket from IdsTariffManager
     *
     * @param paths non-transferable paths we are looking tarif for
     * @param weekend is weekend?
     * @param countAdults count of adults
     * @param countChildren count of children
     * @return optimal ticket for ids
     */
    private OptimalTicket getIdsPrice(List<Path> paths, boolean weekend, int countAdults, int countChildren)
    {
        try
        {
            return idsManager.getPrice(paths, weekend, countAdults, countChildren);
        } catch (IllegalEntityException | ValidationException ex)//(ParserConfigurationException | SAXException | IOException | XPathExpressionException ex)
        {
            throw new ServiceException("Error during retreaving prices from ids xml db.", ex);
        }
    }

    /**
     * Return if the day (specified by date) is working or not
     *
     * @param calendar calendar containing date of the day
     * @return true if the day is working day, false otherwise
     */
    private boolean isWorkingDay(Calendar calendar)
    {
        if (calendar == null)
        {
            throw new IllegalArgumentException("calendar is null");
        }

        int day = calendar.get(Calendar.DAY_OF_WEEK);

        if (day == Calendar.SATURDAY || day == Calendar.SUNDAY)
        {
            return false;
        }

        calendar.set(Calendar.YEAR, GregorianCalendar.getInstance().get(Calendar.YEAR));

        return !notWorkingDay.contains(calendar);
    }

    /**
     * Convert train connection to paths
     *
     * @param trainConnection train connection for one path
     * @return list of non-transferable paths
     */
    private List<Path> trainConnectionToPaths(CdTrainConnection trainConnection)
    {
        List<Path> paths = new ArrayList<>();
        List<CdTrainConnection.TrainStation> stations = trainConnection.getStations();

        for (int i = 1; i < stations.size(); i++)
        {
            //System.out.println("F:" + stations.get(i - 1).getName() + "*" + stations.get(i).getName() + "*");
            Path path;
            try
            {
                path = getPath(stations.get(i - 1).getName(), stations.get(i).getName());//new Path();
            } catch (ServiceException e)
            {
                return Collections.EMPTY_LIST;
            }

            if (path == null)
            {
                return Collections.EMPTY_LIST;
            }

            Time depTime = stations.get(i - 1).getDepartueTime();
            Time arrTime = stations.get(i).getArrivalTime();

            long totalTime = arrTime == null || depTime == null ? Long.MAX_VALUE : arrTime.getTime() - depTime.getTime();

            path.setTotalMinutes((int) TimeUnit.MINUTES.convert(totalTime, TimeUnit.MILLISECONDS));

            paths.add(path);
        }
        //System.out.println("----");
        return paths;
    }

    /**
     * Factory metod for creation of Train instance with information from
     * CdTrainConnection
     *
     * @param trainConnection CdTrainConnection instance with information from
     * cd web page
     * @return new Train instance
     */
    private Train newTrain(CdTrainConnection trainConnection)
    {
        Train train = new Train();
        train.setStations(trainConnection.getStations());
        /*train.setArrivalTime(trainConnection.getStations().get(trainConnection.getStations().size()-1).getArrivalTime());
         train.setDepartueTime(trainConnection.getStations().get(0).getDepartueTime());
         train.setLinkId(trainConnection.getLinkId());
         train.setTrainId(trainConnection.getTrainId());
         */
        return train;
    }

//    public static void main(String[] args) throws IOException, URISyntaxException
//    {
//        TariffManager tm = new TariffManager();
//        Calendar cal = new GregorianCalendar(2014, 4, 16);
//
//        Map<Train, Prices> items = tm.computePrices("Babice n.Svitavou", "Rohatec zast.", cal, 3, 5);
//        for (Entry<Train, Prices> item : items.entrySet())
//        {
//            for (CdTrainConnection.TrainStation stat : item.getKey().getStations())
//            {
//                System.out.println(stat.getName() + "  " + stat.getArrivalTime() + " : " + stat.getDepartueTime() + "   " + stat.getTrainId() + "[" + stat.getLinkId() + "]");
//            }
//
//            System.out.println("\nCD cena celkem: " + item.getValue().getCdTicket().getTotalGroupPrice());
//            for (CdTickets.Ticket tic : item.getValue().getCdTicket().getTickets())
//            {
//                System.out.println("   nazev jizdenky: " + tic.getTicketName() + "\n    cena jizdenky" + tic.getTicketPrice());
//            }
//            System.out.println("IDS cena celkem: " + item.getValue().getIdsTicket().getPrice());
//            System.out.println("   popis jak ziskat jizdenky: " + item.getValue().getIdsTicket().getDescription());
//            System.out.println("-----");
//        }
//    }

}
