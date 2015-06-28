/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend.test;

import backend.IPathManager;
import backend.IllegalEntityException;
import backend.PathManager;
import backend.entity.Path;
import backend.entity.Station;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Tomáš
 */
public class PathManagerTest
{

    private IPathManager manager;

    @Before
    public void setUp() throws URISyntaxException
    {
        manager = new PathManager();
    }

    @Test
    public void getAllStations() throws URISyntaxException, ParserConfigurationException, SAXException, IOException, XPathExpressionException
    {
        final Set<Station> stations = manager.getAllStations();

        assertNotNull("Set of stations cannot be null.", stations);
        assertTrue("Stations cannot be empty.", !stations.isEmpty());

        final URI xml = manager.getClass().getResource("resources/linksIDS.xml").toURI();

        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final Document doc = builder.parse(new File(xml));
        final XPath xpath = XPathFactory.newInstance().newXPath();

        final NodeList expectedStationsXpath = (NodeList) xpath.evaluate("//station/name", doc, XPathConstants.NODESET);
        Set<String> expectedStations = new HashSet<>();

        for (int i = 0; i < expectedStationsXpath.getLength(); i++)
        {
            expectedStations.add(expectedStationsXpath.item(i).getTextContent());
        }
        
        assertTrue("Expected stations cannot be empty.", !expectedStations.isEmpty());
        assertEquals(expectedStations, stations);
        assertNotSame(expectedStations, stations);
        //assertDeepEquals(expectedStations, stations);
    }
    
    // Special test for linksIDS.xml
    @Test
    public void getPathTest1(){
        Path path = manager.getPath("Brno hl.n.","Blansko-mesto");
        assertNotNull("Path cannot be null.", path);
        assertEquals(path.getTotalMinutes(),null);
        assertEquals(path.isBrno(),true);
        assertDeepEquals(path.getZones(),Arrays.asList(100,215,225,235));
        assertDeepEquals(path.getStations(),Arrays.asList("Brno hl.n.",
                "Brno-Židenice","Bílovice n.Svitavou","Babice n.Svitavou",
                "Adamov","Adamov zast.","Blansko","Blansko město"));
    }
    
    // Special test for linksIDS.xml
    @Test
    public void getPathTest2(){
        Path path = manager.getPath("Nihov","Kurim");
        assertNotNull("Path cannot be null.", path);
        assertEquals(path.getTotalMinutes(),null);
        assertEquals(path.isBrno(),false);
        assertDeepEquals(path.getZones(),Arrays.asList(345,340,330,320,310));
        assertDeepEquals(path.getStations(),Arrays.asList("Níhov",
                "Řikonín","Dolní Loučky","Tišnov","Hradčany","Čebín","Kuřim"));
    }
    
    // Special test for linksIDS.xml
    @Test
    public void getPathTest3(){
        Path path = manager.getPath("Brno hl.n.","Radostice");
        assertNotNull("Path cannot be null.", path);
        assertEquals(path.getTotalMinutes(),null);
        assertEquals(path.isBrno(),true);
        assertDeepEquals(path.getZones(),Arrays.asList(100,101,410,427));
        assertDeepEquals(path.getStations(),Arrays.asList("Brno hl.n.",
                "Brno-Horní Heršpice","Troubsko","Střelice dolní","Střelice","Radostice"));
    }
    
    // Special test for linksIDS.xml
    @Test
    public void getPathTest4(){
        Path path = manager.getPath("Radostice","Bohutice");
        assertNotNull("Path cannot be null.", path);
        assertEquals(path.getTotalMinutes(),null);
        assertEquals(path.isBrno(),false);
        assertDeepEquals(path.getZones(),Arrays.asList(427,437,448,459));
        assertDeepEquals(path.getStations(),Arrays.asList("Radostice","Silůvky",
                "Moravské Bránice","Moravský Krumlov","Rakšice","Bohutice"));
    }
    
    // Special test for linksIDS.xml
    @Test
    public void getPathTest5(){
        Path path = manager.getPath("Oslavany","Ivančice letovisko");
        assertNotNull("Path cannot be null.", path);
        assertEquals(path.getTotalMinutes(),null);
        assertEquals(path.isBrno(),false);
        assertDeepEquals(path.getZones(),Arrays.asList(447));
        assertDeepEquals(path.getStations(),Arrays.asList("Ivančice město",
                "Ivančice","Oslavany","Ivančice letovisko"));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getPathWithNullArg()
    {
        manager.getPath(null, "to");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getPathWithNullArg2()
    {
        manager.getPath("from", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getPathWithEmptyArg()
    {
        manager.getPath("", "to");
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void getPathWithEmptyArg2()
    {
        manager.getPath("from", "");
    }
    
    @Test(expected = IllegalEntityException.class)
    public void getPathWithWrongArg()
    {
        manager.getPath("from", "to");
    }

    private static void assertDeepEquals(Set<String> expectedStations, Set<String> stations)
    {
        stations.removeAll(expectedStations);
        
        assertTrue("Stations should be same as expected stations", stations.isEmpty());
    }
    
    private static void assertDeepEquals(List<?> zones, List<?> expectedZones)
    {
        zones.removeAll(expectedZones);
        
        assertTrue("Stations should be same as expected stations", zones.isEmpty());
    }
   
}
