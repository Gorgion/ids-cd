/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import backend.entity.OptimalTicket;
import backend.entity.Path;
import java.util.List;

/**
 * This interface represents tariff manager for computation IDS JMK price and
 * tariff.
 *
 * @author ids-cd team
 */
public interface IIdsTariffManager {

    /**
     * Return price according IDS JMK tariff XML file for a given path
     *
     * @param paths list of non-transferable paths
     * @param weekend true if non working day, false otherwise
     * @param countAdults count adults
     * @param countChildren count children under 15 years
     * @return the cheapest ticket for given parameters of group
     * @throws ServiceException when XML DB operation fails
     * @throws ValidationException when XML data source breaks validation rules
     * @throws IllegalEntityException when path breaks validation rules
     */
    OptimalTicket getPrice(List<Path> paths, boolean weekend, int countAdults, int countChildren);
}
