/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This entity class represents price given by IDS JMK tariff. IdsPrice is given
 * by its value, zone count limit, total time limit in minutes, station limit
 * and variable for indicating zones 100 and 101.
 *
 * @author ids-cd team
 */
public class IdsPrice {

    private Integer value;
    private Integer zoneCountLimit;
    private Integer timeLimit;
    private Integer stationLimit;
    private String except;
    private TicketType type;
    private Integer limAdult;
    private Integer limChildren;
    List<Integer> inZones = new ArrayList<>();

    public List<Integer> getInZones() {
        return inZones;
    }

    public void setInZones(List<Integer> inZones) {
        this.inZones = inZones;
    }

    public Integer getLimAdult() {
        return limAdult;
    }

    public void setLimAdult(Integer limAdult) {
        this.limAdult = limAdult;
    }

    public Integer getLimChildren() {
        return limChildren;
    }

    public void setLimChildren(Integer limChildren) {
        this.limChildren = limChildren;
    }

    public TicketType getType() {
        return type;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    public String getExcept() {
        return except;
    }

    public void setExcept(String except) {
        this.except = except;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Integer getZoneCountLimit() {
        return zoneCountLimit;
    }

    public void setZoneCountLimit(Integer areaCountLimit) {
        this.zoneCountLimit = areaCountLimit;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }

    public Integer getStationLimit() {
        return stationLimit;
    }

    public void setStationLimit(Integer stationLimit) {
        this.stationLimit = stationLimit;
    }

    @Override
    public String toString() {
        String str = "";
        if (zoneCountLimit != null) {
            str = (zoneCountLimit == Integer.MAX_VALUE) ? " bez limitu" : zoneCountLimit.toString();
        }
        String info = "IDS JMK listok: " + System.lineSeparator() + "\t limit na pocet zon: " + str + System.lineSeparator();
        info += "\t cena: " + value + System.lineSeparator();
        if (timeLimit != null) {
            info += "\t casovy limit: " + timeLimit + " minut" + System.lineSeparator();
        }
        if (stationLimit != null) {
            info += "\t maximalny pocet zastavok: " + stationLimit + System.lineSeparator();
        }
        if (except != null) {
            info += "\t okrem zon: " + 100 + ", " + 101 + System.lineSeparator();
        }
        if (limAdult != null) {
            info += "\t maximalny pocet dospelych je " + limAdult + " a  " + limChildren + " deti" + System.lineSeparator();
        }
        if (type != null) {
            info += "\t typ listku je " + type.toString() + System.lineSeparator();
        }
        if (inZones != null && !inZones.isEmpty()) {
            info += "\t v zonach: ";
            for (Integer i : inZones) {
                info += i + " ";
            }
            info += System.lineSeparator();
        }
        return info;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj instanceof IdsPrice) {

            final IdsPrice other = (IdsPrice) obj;

            if (!Objects.equals(other.value, value)) {
                return false;
            }
            if (!Objects.equals(other.zoneCountLimit, zoneCountLimit)) {
                return false;
            }
            if (!Objects.equals(other.timeLimit, timeLimit)) {
                return false;
            }
            if (!Objects.equals(other.stationLimit, stationLimit)) {
                return false;
            }
            if (!Objects.equals(other.except, except)) {
                return false;
            }
            if (!Objects.equals(other.type, type)) {
                return false;
            }
            if (!Objects.equals(other.limAdult, limAdult)) {
                return false;
            }
            if (!Objects.equals(other.limChildren, limChildren)) {
                return false;
            }
            if (!Objects.equals(other.inZones, inZones)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.value);
        hash = 97 * hash + Objects.hashCode(this.zoneCountLimit);
        hash = 97 * hash + Objects.hashCode(this.timeLimit);
        hash = 97 * hash + Objects.hashCode(this.stationLimit);
        hash = 97 * hash + Objects.hashCode(this.except);
        hash = 97 * hash + Objects.hashCode(this.type);
        hash = 97 * hash + Objects.hashCode(this.limAdult);
        hash = 97 * hash + Objects.hashCode(this.limChildren);
        hash = 97 * hash + Objects.hashCode(this.inZones);
        return hash;
    }

    public static enum TicketType {

        BASIC,
        WEEKEND_GROUP;
        
        @Override
        public String toString() {
            if(name().equals("BASIC"))
                return "zakladny listok";
            if(name().equals("WEEKEND_GROUP"))
                return "vikendovy skupinovy listok";
            return name();
        }
    }

}
