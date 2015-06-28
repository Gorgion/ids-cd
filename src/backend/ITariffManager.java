/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backend;

import backend.entity.Prices;
import backend.entity.Train;
import java.util.Calendar;
import java.util.Map;

/**
 *
 * @author Tomáš
 */
public interface ITariffManager
{

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
    Map<Train, Prices> computePrices(String from, String to, Calendar calendar, int adultsCount, int childrenCount);
    
}
