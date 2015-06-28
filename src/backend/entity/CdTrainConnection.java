/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.entity;

import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Tomáš
 */
public class CdTrainConnection
{

    private BigDecimal value;
    private List<TrainStation> stations;

    public List<TrainStation> getStations()
    {
        return stations;
    }

    public void setStations(List<TrainStation> stations)
    {
        this.stations = stations;
    }       

    public BigDecimal getValue()
    {
        return value;
    }

    public void setValue(BigDecimal value)
    {
        this.value = value;
    }    

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.value);
        hash = 17 * hash + Objects.hashCode(this.stations);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final CdTrainConnection other = (CdTrainConnection) obj;
        if (!Objects.equals(this.value, other.value))
        {
            return false;
        }
        if (!Objects.equals(this.stations, other.stations))
        {
            return false;
        }
        return true;
    }
    
    

    public static class TrainStation
    {

        private String name;        
        private Time departueTime;
        private Time arrivalTime;
        private String linkId;
        private String trainId;

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }
        
        public Time getDepartueTime()
        {
            return departueTime;
        }

        public void setDepartueTime(Time departueTime)
        {
            this.departueTime = departueTime;
        }

        public Time getArrivalTime()
        {
            return arrivalTime;
        }

        public void setArrivalTime(Time arrivalTime)
        {
            this.arrivalTime = arrivalTime;
        }

        public String getLinkId()
        {
            return linkId;
        }

        public void setLinkId(String linkId)
        {
            this.linkId = linkId;
        }

        public String getTrainId()
        {
            return trainId;
        }

        public void setTrainId(String trainId)
        {
            this.trainId = trainId;
        }

        @Override
        public int hashCode()
        {
            int hash = 3;
            hash = 41 * hash + Objects.hashCode(this.name);
            hash = 41 * hash + Objects.hashCode(this.departueTime);
            hash = 41 * hash + Objects.hashCode(this.arrivalTime);
            hash = 41 * hash + Objects.hashCode(this.linkId);
            hash = 41 * hash + Objects.hashCode(this.trainId);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final TrainStation other = (TrainStation) obj;
            if (!Objects.equals(this.name, other.name))
            {
                return false;
            }
            if (!Objects.equals(this.departueTime, other.departueTime))
            {
                return false;
            }
            if (!Objects.equals(this.arrivalTime, other.arrivalTime))
            {
                return false;
            }
            if (!Objects.equals(this.linkId, other.linkId))
            {
                return false;
            }
            if (!Objects.equals(this.trainId, other.trainId))
            {
                return false;
            }
            return true;
        }
        
        
    }
}
