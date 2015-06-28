/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package backend.test;

import backend.ITariffManager;
import backend.TariffManager;
import backend.entity.Prices;
import backend.entity.Train;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Tomáš
 */
public class TariffManagerTest
{
    private ITariffManager manager;
    
    public TariffManagerTest()
    {
    }
    
    @Before
    public void setUp() throws URISyntaxException
    {
        manager = new TariffManager();
    }
    
    @Test
    public void getBasicGroupTicket()
    {
        Map<Train, Prices> prices = manager.computePrices("Níhov", "Ladná", new GregorianCalendar(new Locale("cs","CZ")), 3, 3);
        
        assertNotNull("Collection cannot be null.", prices);        
        assertTrue("Collection cannot be empty.", !prices.isEmpty());
        
        for(Entry<Train, Prices> item : prices.entrySet())
        {
            assertNotNull("Station cannot be null.", item.getKey());
            assertTrue("Station cannot be empty.", !item.getKey().getStations().isEmpty());
            
            assertNotNull("Prices cannot be null.", item.getValue());
            
            assertNotNull("Cd ticket cannot be null.", item.getValue().getCdTicket());
            assertNotNull("Cd ticket price cannot be null.", item.getValue().getCdTicket().getTotalGroupPrice());
            assertNotNull("Cd tickets cannot be null.", item.getValue().getCdTicket().getTickets());
            assertTrue("Cd tickets cannot be empty.", !item.getValue().getCdTicket().getTickets().isEmpty());
            
            assertNotNull("Ids ticket cannot be null.", item.getValue().getIdsTicket());
            assertNotNull("Ids ticket cannot be null.", item.getValue().getIdsTicket().getPrice());
            assertNotNull("Ids ticket cannot be null.", item.getValue().getIdsTicket().getDescription());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithNullArg()
    {
        manager.computePrices(null, "to", new GregorianCalendar(), 10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithNullArg2()
    {
        manager.computePrices("from", null, new GregorianCalendar(), 10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithNullArg3()
    {
        manager.computePrices("from", "to", null, 10, 10);
    }
        
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithEmptyArg()
    {
        manager.computePrices("", "to", new GregorianCalendar(), 10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithEmptyArg2()
    {
        manager.computePrices("from", "", new GregorianCalendar(), 10, 10);
    }   
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithSameFromToArg()
    {
        manager.computePrices("to", "to", new GregorianCalendar(), 10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithNegativeArg()
    {
        manager.computePrices("from", "to", new GregorianCalendar(), -10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithNegativeArg2()
    {
        manager.computePrices("from", "to", new GregorianCalendar(), 10, -10);
    }        
    
    @Test(expected = IllegalArgumentException.class)
    public void computePricesWithZeroCountArg()
    {
        manager.computePrices("from", "to", new GregorianCalendar(), 0, 0);
    }
}
