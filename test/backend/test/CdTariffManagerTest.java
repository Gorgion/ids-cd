/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.test;

import backend.CdTariffManager;
import backend.ICdTariffManager;
import backend.entity.CdTickets;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Tomáš
 */
public class CdTariffManagerTest
{

    private ICdTariffManager cdMgr;

    public CdTariffManagerTest()
    {
    }

    @Before
    public void setUp() throws URISyntaxException
    {
        cdMgr = new CdTariffManager();
    }
    
    @Test
    public void getBasicGroupTicket()
    {
        CdTickets tic = cdMgr.getBasicGroupPrice(BigDecimal.TEN, 5);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        assertEquals(BigDecimal.valueOf(33), tic.getTotalGroupPrice());
        
        tic = cdMgr.getBasicGroupPrice(BigDecimal.TEN, 1);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        assertEquals(BigDecimal.valueOf(10), tic.getTotalGroupPrice());                
    }

    @Test(expected = IllegalArgumentException.class)
    public void getBasicGroupTicketWithNullArg()
    {
        cdMgr.getBasicGroupPrice(null, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getBasicGroupTicketWithNegativeArg()
    {
        cdMgr.getBasicGroupPrice(BigDecimal.TEN, -1);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getBasicGroupTicketWithNegativeArg2()
    {
        cdMgr.getBasicGroupPrice(BigDecimal.TEN.negate(), 2);
    }        
    
    @Test(expected = IllegalArgumentException.class)
    public void getBasicGroupTicketWithZeroCountArg()
    {
        cdMgr.getMixedGroupPrice(BigDecimal.TEN, 0, 0);
    }

    @Test
    public void getMixedGroupTicket()
    {
        CdTickets tic = cdMgr.getMixedGroupPrice(BigDecimal.TEN, 5, 6);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        assertEquals(BigDecimal.valueOf(63), tic.getTotalGroupPrice());
        
        tic = cdMgr.getMixedGroupPrice(BigDecimal.TEN, 1, 0);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        assertEquals(BigDecimal.valueOf(10), tic.getTotalGroupPrice());
        
        tic = cdMgr.getMixedGroupPrice(BigDecimal.TEN, 0, 1);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        assertEquals(BigDecimal.valueOf(5), tic.getTotalGroupPrice());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getMixedGroupTicketWithNullArg()
    {
        cdMgr.getMixedGroupPrice(null, 10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getMixedGroupTicketWithNegativeArg()
    {
        cdMgr.getMixedGroupPrice(BigDecimal.TEN, -1, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getMixedGroupTicketWithNegativeArg2()
    {
        cdMgr.getMixedGroupPrice(BigDecimal.TEN, 2, -10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getMixedGroupTicketWithNegativeArg3()
    {
        cdMgr.getMixedGroupPrice(BigDecimal.TEN.negate(), 10, 10);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getMixedGroupTicketWithZeroCountArg()
    {
        cdMgr.getMixedGroupPrice(BigDecimal.TEN, 0, 0);
    }

    @Test
    public void getAdvancedGroupTariffs()
    {
        CdTickets tic = cdMgr.getAdvancedGroupTariffs(5, 6, true, 12);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        tic = cdMgr.getAdvancedGroupTariffs(1, 0, false, 12);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
        
        tic = cdMgr.getAdvancedGroupTariffs(0, 1, true, 12);
        assertNotNull("CdTickets cannot be null.", tic);
        assertNotNull("Price cannot be null.", tic.getTotalGroupPrice());
        assertNotNull("Tickets cannot be null.", tic.getTickets());
        assertTrue("Tickets cannot be empty.", !tic.getTickets().isEmpty());
    }    
    
    @Test(expected = IllegalArgumentException.class)
    public void getAdvancedGroupTicketWithNegativeArg()
    {
        cdMgr.getAdvancedGroupTariffs(0, -1, false, 12);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getAdvancedGroupTicketWithNegativeArg2()
    {
        cdMgr.getAdvancedGroupTariffs(-10, 0, true, 12);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getAdvancedGroupTicketWithNegativeArg3()
    {
        cdMgr.getAdvancedGroupTariffs(0, 1, false, -12);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getAdvancedGroupTicketWithZeroCountArg()
    {
        cdMgr.getAdvancedGroupTariffs(0, 0, true, 0);
    }
}
