/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.entity;

import java.util.List;
import java.util.Objects;

/**
 * This entity class represents Path. Path is given by stations, zone count,
 * total time in minutes and boolean variable for indicating zones 100 and 101.
 *
 * @author ids-cd team
 */
public class Path {

    private List<String> stations;
    private Integer totalMinutes;
    private boolean brno;
    private List<Integer> zones;

    public List<Integer> getZones() {
        return zones;
    }

    public void setZones(List<Integer> zones) {
        this.zones = zones;
    }

    public boolean isBrno() {
        return brno;
    }

    public void setBrno(boolean brno) {
        this.brno = brno;
    }

    public List<String> getStations() {
        return stations;
    }

    public void setStations(List<String> stations) {
        this.stations = stations;
    }

    public Integer getTotalMinutes() {
        return totalMinutes;
    }

    public void setTotalMinutes(Integer totalMinutes) {
        this.totalMinutes = totalMinutes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Path) {
            final Path other = (Path) obj;
            if (!Objects.equals(other.brno, brno)) {
                return false;
            }
            if (!Objects.equals(other.stations, stations)) {
                return false;
            }
            if (!Objects.equals(other.totalMinutes, totalMinutes)) {
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.stations);
        hash = 79 * hash + this.totalMinutes;
        hash = 79 * hash + (this.brno ? 1 : 0);
        return hash;
    }
}
