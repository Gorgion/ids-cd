/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.test;

import backend.IdsTariffManager;
import backend.IllegalEntityException;
import backend.PathManager;
import backend.entity.IdsPrice;
import backend.entity.OptimalGroup;
import backend.entity.OptimalTicket;
import backend.entity.Path;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Tomáš
 */
public class IdsTariffManagerTest {

    private IdsTariffManager manager;

    public IdsTariffManagerTest() {
    }

    @Before
    public void setUp() throws URISyntaxException {
        manager = new IdsTariffManager();
    }

    @Test
    public void getPrice1() throws URISyntaxException {
        final String from = "Níhov";
        final String to = "Tišnov";
        final List<Path> paths = new ArrayList<>();
        final Path path = (new PathManager()).getPath(from, to);
        path.setTotalMinutes(40);
        paths.add(path);

        final OptimalTicket ticket = manager.getPrice(paths, true, 5, 6);

        assertNotNull("OptimalTicket cannot be null.", ticket);
        assertNotNull("Ticket description cannot be null.", ticket.getDescription());
        assertTrue("Ticket description cannot be empty.", !ticket.getDescription().isEmpty());
        assertNotNull("Ticket price cannot be null.", ticket.getPrice());
        assertTrue("Ticket price must be positive.", ticket.getPrice().signum() == 1);
        assertEquals(ticket.getPrice(), new BigDecimal(213));
        System.out.println(ticket.getDescription());
    }

    @Test
    public void getPrice2() throws URISyntaxException {
        final String from = "Blansko";
        final String to = "Letovice";
        final List<Path> paths = new ArrayList<>();
        final Path path = (new PathManager()).getPath(from, to);
        path.setTotalMinutes(30);
        paths.add(path);

        final OptimalTicket ticket = manager.getPrice(paths, true, 5, 6);

        assertNotNull("OptimalTicket cannot be null.", ticket);
        assertNotNull("Ticket description cannot be null.", ticket.getDescription());
        assertTrue("Ticket description cannot be empty.", !ticket.getDescription().isEmpty());
        assertNotNull("Ticket price cannot be null.", ticket.getPrice());
        assertTrue("Ticket price must be positive.", ticket.getPrice().signum() == 1);
        assertEquals(ticket.getPrice(), new BigDecimal(242));
        System.out.println(ticket.getDescription());
    }

    @Test
    public void getPrice3() throws URISyntaxException {
        final String from = "Brno hl.n.";
        final String to = "Brno-Slatina";
        final List<Path> paths = new ArrayList<>();
        final Path path = (new PathManager()).getPath(from, to);
        path.setTotalMinutes(13);
        paths.add(path);

        final OptimalTicket ticket = manager.getPrice(paths, true, 5, 6);

        assertNotNull("OptimalTicket cannot be null.", ticket);
        assertNotNull("Ticket description cannot be null.", ticket.getDescription());
        assertTrue("Ticket description cannot be empty.", !ticket.getDescription().isEmpty());
        assertNotNull("Ticket price cannot be null.", ticket.getPrice());
        assertTrue("Ticket price must be positive.", ticket.getPrice().signum() == 1);
        assertEquals(ticket.getPrice(), new BigDecimal(128));
        System.out.println(ticket.getDescription());
    }

    @Test
    public void getPrice4() throws URISyntaxException {
        final String from = "Brno-Horní Heršpice";
        final String to = "Brno-Řečkovice";
        final List<Path> paths = new ArrayList<>();
        final Path path = (new PathManager()).getPath(from, to);
        path.setTotalMinutes(70);
        paths.add(path);

        final OptimalTicket ticket = manager.getPrice(paths, true, 5, 6);

        assertNotNull("OptimalTicket cannot be null.", ticket);
        assertNotNull("Ticket description cannot be null.", ticket.getDescription());
        assertTrue("Ticket description cannot be empty.", !ticket.getDescription().isEmpty());
        assertNotNull("Ticket price cannot be null.", ticket.getPrice());
        assertTrue("Ticket price must be positive.", ticket.getPrice().signum() == 1);
        assertEquals(ticket.getPrice(), new BigDecimal(207));
        System.out.println(ticket.getDescription());
    }

    @Test
    public void getPrice5() throws URISyntaxException {
        final String from = "Brno hl.n.";
        final String to = "Hodonin";
        final List<Path> paths = new ArrayList<>();
        final Path path = (new PathManager()).getPath(from, to);
        path.setTotalMinutes(65);
        paths.add(path);

        final OptimalTicket ticket = manager.getPrice(paths, true, 5, 6);

        assertNotNull("OptimalTicket cannot be null.", ticket);
        assertNotNull("Ticket description cannot be null.", ticket.getDescription());
        assertTrue("Ticket description cannot be empty.", !ticket.getDescription().isEmpty());
        assertNotNull("Ticket price cannot be null.", ticket.getPrice());
        assertTrue("Ticket price must be positive.", ticket.getPrice().signum() == 1);
        assertEquals(ticket.getPrice(), new BigDecimal(466));
        System.out.println(ticket.getDescription());
    }

    @Test
    public void getPrice6() throws URISyntaxException {
        final String from = "Brno hl.n.";
        final String to = "Hodonin";
        final List<Path> paths = new ArrayList<>();
        final Path path = (new PathManager()).getPath(from, to);
        path.setTotalMinutes(65);
        paths.add(path);

        final OptimalTicket ticket = manager.getPrice(paths, false, 5, 6);

        assertNotNull("OptimalTicket cannot be null.", ticket);
        assertNotNull("Ticket description cannot be null.", ticket.getDescription());
        assertTrue("Ticket description cannot be empty.", !ticket.getDescription().isEmpty());
        assertNotNull("Ticket price cannot be null.", ticket.getPrice());
        assertTrue("Ticket price must be positive.", ticket.getPrice().signum() == 1);
        assertEquals(ticket.getPrice(), new BigDecimal(688));
        System.out.println(ticket.getDescription());
    }

    @Test
    public void getOptimalGroupTest1() {
        List<IdsPrice> prices = new ArrayList<>();
        IdsPrice p = new IdsPrice();
        p.setValue(90);
        p.setLimAdult(3);
        p.setLimChildren(2);
        prices.add(p);
        p = new IdsPrice();
        p.setValue(20);
        assertEquals(manager.getOptimalGroup(prices, p, 10, 6), new OptimalGroup(0, 10, 6, p, 260));
    }

    @Test
    public void getOptimalGroupTest2() {
        List<IdsPrice> prices = new ArrayList<>();
        IdsPrice p = new IdsPrice();
        p.setValue(90);
        p.setLimAdult(3);
        p.setLimChildren(2);
        prices.add(p);
        IdsPrice price = new IdsPrice();
        price.setValue(30);
        assertEquals(manager.getOptimalGroup(prices, price, 10, 6), new OptimalGroup(3, 1, 0, p, 300));
    }

    @Test
    public void getOptimalGroupTest3() {
        List<IdsPrice> prices = new ArrayList<>();
        IdsPrice p = new IdsPrice();
        p.setValue(90);
        p.setLimAdult(3);
        p.setLimChildren(2);
        prices.add(p);
        IdsPrice price = new IdsPrice();
        price.setValue(30);
        assertEquals(manager.getOptimalGroup(prices, price, 10, 5), new OptimalGroup(3, 1, 0, p, 300));
    }

    @Test
    public void getOptimalGroupTest4() {
        List<IdsPrice> prices = new ArrayList<>();
        IdsPrice p = new IdsPrice();
        p.setValue(90);
        p.setLimAdult(3);
        p.setLimChildren(2);
        prices.add(p);
        IdsPrice price = new IdsPrice();
        price.setValue(25);
        assertEquals(manager.getOptimalGroup(prices, price, 10, 5), new OptimalGroup(2, 4, 1, p, 292));
    }

    @Test
    public void getOptimalGroupTest5() {
        List<IdsPrice> prices = new ArrayList<>();
        IdsPrice p = new IdsPrice();
        p.setValue(90);
        p.setLimAdult(3);
        p.setLimChildren(2);
        prices.add(p);
        IdsPrice price = new IdsPrice();
        price.setValue(30);
        assertEquals(manager.getOptimalGroup(prices, price, 10, 3), new OptimalGroup(3, 1, 0, p, 300));
    }

    @Test
    public void getOptimalGroupTest6() {
        List<IdsPrice> prices = new ArrayList<>();
        IdsPrice p = new IdsPrice();
        p.setValue(50);
        p.setLimAdult(3);
        p.setLimChildren(2);
        prices.add(p);
        IdsPrice price = new IdsPrice();
        price.setValue(30);
        assertEquals(manager.getOptimalGroup(prices, price, 11, 3), new OptimalGroup(4, 0, 0, p, 200));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getPriceWithNullArg() {
        manager.getPrice(null, true, 10, 10);
    }

    @Test(expected = IllegalEntityException.class)
    public void getPriceWithEmptyArg() {
        manager.getPrice(Collections.EMPTY_LIST, true, 10, 10);
    }

    @Test(expected = IllegalEntityException.class)
    public void getPriceWithNegativeArg() {
        List<Path> paths = new ArrayList<>();
        paths.add(new Path());

        manager.getPrice(paths, true, -10, 2);
    }

    @Test(expected = IllegalEntityException.class)
    public void getPriceWithNegativeArg2() {
        List<Path> paths = new ArrayList<>();
        paths.add(new Path());

        manager.getPrice(paths, true, 10, -2);
    }
}
