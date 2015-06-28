/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package backend;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 *
 * @author Tomáš
 */
public class XmlValidator
{

    /**
     * Validates given XML file according to given XSD file
     *
     * @param xmlFile XML file
     * @param xsdFile XML schema file
     * @throws ValidationException when xmlFile is not valid according to
     * xsdFile
     */
    public static void validateWithXMLSchema(File xmlFile, File xsdFile) throws ValidationException
    {
        if (xmlFile == null || xsdFile == null)
        {
            throw new IllegalArgumentException("XML file or XSD file is null");
        }

        try
        {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(xsdFile);

            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(xmlFile));
        } catch (IOException | SAXException ex)
        {
            throw new ValidationException("Data source is not valid XML file!", ex);
        }
    }
    
    /**
     * Validates given XML file according to given XSD file
     *
     * @param xmlFilename filename of the XML file
     * @param xsdFilename filename of the XML schema file
     * @throws ValidationException when xmlFile is not valid according to
     * xsdFile
     */
    public static void validateWithXMLSchema(String xmlFilename, String xsdFilename) throws ValidationException
    {
        if (xmlFilename == null || xsdFilename == null)
        {
            throw new IllegalArgumentException("XML file or XSD file is null");
        }
        
        validateWithXMLSchema(new File(xmlFilename), new File(xsdFilename));
    }

//    public static void main(String[] args) throws URISyntaxException, IOException
//    {
//        XmlValidator xv = new XmlValidator();
//        URI xmlUri = xv.getClass().getResource("resources/cdAdvancedGroupTariffs.xml").toURI();
//        URI xsdUri = xv.getClass().getResource("resources/cdAdvancedGroupTariffs.xsd").toURI();
//
//        File xml = new File(xmlUri);
//        File xsd = new File(xsdUri);
//
//        XmlValidator.validateWithXMLSchema(xml, xsd);
//
//        System.out.println("Ok");
//    }
}
