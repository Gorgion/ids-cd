/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import backend.entity.Path;
import backend.entity.Station;
import java.util.Set;

/**
 * Interface IPathManager determinate stations of path
 *
 * @author ids-cd team
 */
public interface IPathManager {

    /**
     * Returns set of all stations in IDS JMK
     *
     * @return set of all stations
     * @throws ServiceException when XML DB operation fails
     */
    Set<Station> getAllStations() throws ServiceException;

    /**
     * Return path from XML DB.
     *
     * @param from starting station of searching path
     * @param to ending station of searching path
     * @return Path from DB with given data.
     * @throws ServiceException when XML DB operation fails
     * @throws ValidationException when XML data source breaks validation rules
     * @throws IllegalEntityException when there does not exists
     * non-transferable link in IDS JMK with stations from and to
     */
    Path getPath(String from, String to) throws ServiceException, ValidationException, IllegalEntityException;

}
