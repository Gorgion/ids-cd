/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backend.entity;

import java.sql.Time;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Tomáš
 */
public class Train
{
    private List<CdTrainConnection.TrainStation> stations;

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.stations);
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
        final Train other = (Train) obj;
        if (!Objects.equals(this.stations, other.stations))
        {
            return false;
        }
        return true;
    }
    
    /*private String linkId;
    private String trainId;   

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.linkId);
        hash = 41 * hash + Objects.hashCode(this.trainId);
        hash = 41 * hash + Objects.hashCode(this.departueTime);
        hash = 41 * hash + Objects.hashCode(this.arrivalTime);
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
        final Train other = (Train) obj;
        if (!Objects.equals(this.linkId, other.linkId))
        {
            return false;
        }
        if (!Objects.equals(this.trainId, other.trainId))
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
        return true;
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
    private Time departueTime;
    private Time arrivalTime;*/

    public List<CdTrainConnection.TrainStation> getStations()
    {
        return stations;
    }

    public void setStations(List<CdTrainConnection.TrainStation> stations)
    {
        this.stations = stations;
    }
}
