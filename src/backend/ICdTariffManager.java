/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backend;

import backend.entity.CdTickets;
import backend.entity.CdTrainConnection;
import backend.entity.Path;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;

/**
 *
 * @author Tomáš
 */
public interface ICdTariffManager
{

    /**
     * Return train collections from CD web app for a given path
     *
     * @param fromStation from station of the path
     * @param toStation to station of the path
     * @param calendar date and time of the path
     * @return collection of train connections
     * @throws java.io.IOException due to retreaving data from web page
     */
    Collection<CdTrainConnection> getTrainConnections(String fromStation, String toStation, Calendar calendar) throws IOException;
 
    /**
     * Calculate basic group cd price for group(s) of people.
     *
     * @param basePrice base price of cd tarif for 1 person
     * @param personCount count of person in the group (may be greater then 30)
     * @return CdTickets for the path
     */
    CdTickets getBasicGroupPrice(BigDecimal basePrice, int personCount);
    
    /**
     * Compute price for mixed group of children and addults with age emphasis
     *
     * @param basicCdPrice basic price for one person
     * @param adultsCount count of adult people
     * @param childrenCount count of children
     * @return CdTickets for the path
     */
    CdTickets getMixedGroupPrice(BigDecimal basicCdPrice, int adultsCount, int childrenCount);
    
    /**
     * Get advanced type of group ticket. These tickets are stored in xml db.
     *
     * @param adultsCount count of adults
     * @param childrenCounts count of children
     * @param weekendOnly ticket is allowed only on weekends
     * @param hours time of the path in hours
     * @return CdTickets for the path with the best params (e.g. lowest price)
     */
    CdTickets getAdvancedGroupTariffs(int adultsCount, int childrenCounts, boolean weekendOnly, int hours);
}
