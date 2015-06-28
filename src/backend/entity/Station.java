/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backend.entity;

/**
 *
 * @author Tomáš
 */
public class Station
{
    private String name;
    private int latitudeDegree;
    private int latitudeMinute;
    private int latitudeSecond;
    private int longitudeDegree;
    private int longitudeMinute;
    private int longitudeSecond;

    public Station(String name, int latitudeDegree, int latitudeMinute, int latitudeSecond, int longitudeDegree, int longitudeMinute, int longitudeSecond)
    {
        this.name = name;
        this.latitudeDegree = latitudeDegree;
        this.latitudeMinute = latitudeMinute;
        this.latitudeSecond = latitudeSecond;
        this.longitudeDegree = longitudeDegree;
        this.longitudeMinute = longitudeMinute;
        this.longitudeSecond = longitudeSecond;
    }

    public Station()
    {
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getLatitudeDegree()
    {
        return latitudeDegree;
    }

    public void setLatitudeDegree(int latitudeDegree)
    {
        this.latitudeDegree = latitudeDegree;
    }

    public int getLatitudeMinute()
    {
        return latitudeMinute;
    }

    public void setLatitudeMinute(int latitudeMinute)
    {
        this.latitudeMinute = latitudeMinute;
    }

    public int getLatitudeSecond()
    {
        return latitudeSecond;
    }

    public void setLatitudeSecond(int latitudeSecond)
    {
        this.latitudeSecond = latitudeSecond;
    }

    public int getLongitudeDegree()
    {
        return longitudeDegree;
    }

    public void setLongitudeDegree(int longitudeDegree)
    {
        this.longitudeDegree = longitudeDegree;
    }

    public int getLongitudeMinute()
    {
        return longitudeMinute;
    }

    public void setLongitudeMinute(int longitudeMinute)
    {
        this.longitudeMinute = longitudeMinute;
    }

    public int getLongitudeSecond()
    {
        return longitudeSecond;
    }

    public void setLongitudeSecond(int longitudeSecond)
    {
        this.longitudeSecond = longitudeSecond;
    }
    
}
