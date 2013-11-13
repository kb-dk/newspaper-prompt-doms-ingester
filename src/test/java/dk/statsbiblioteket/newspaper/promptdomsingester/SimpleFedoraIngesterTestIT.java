package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.newspaper.RecursiveFedoraCleaner;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 *
 */
public class SimpleFedoraIngesterTestIT extends AbstractSimpleFedoraIngesterTest {
    String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";


    @BeforeMethod
    public void setup() throws MalformedURLException, JAXBException, BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException, PIDGeneratorException {
        cleanupFedora();
    }

    @AfterMethod
    public void teardown() throws MalformedURLException, JAXBException, BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException, PIDGeneratorException {
         cleanupFedora();
    }

    public void cleanupFedora() throws MalformedURLException, JAXBException, PIDGeneratorException, BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        String label = "path:B400022028241-RT1";
        RecursiveFedoraCleaner.cleanFedora(getEnhancedFedora(), label, true);
    }

    @Override
    public EnhancedFedora getEnhancedFedora() throws JAXBException, PIDGeneratorException, MalformedURLException {
        Properties props = new Properties();
        try {
            props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Credentials creds = new Credentials(props.getProperty("fedora.admin.username"), props.getProperty("fedora.admin.password"));
        String fedoraLocation = props.getProperty("doms.server");
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, fedoraLocation, props.getProperty("pidgenerator.location") , null);
        return eFedora;
    }

    @Test(groups = "integrationTest")
    @Override
    public void testIngest() throws Exception {
        super.testIngest();
        String pid = super.pid;
        String foundPid = getEnhancedFedora().findObjectFromDCIdentifier("path:B400022028241-RT1").get(0);
        assertEquals(pid, foundPid);
        String nextPid = getEnhancedFedora().findObjectFromDCIdentifier("path:B400022028241-RT1/400022028241-1").get(0);
        List<FedoraRelation> relations = getEnhancedFedora().getNamedRelations(pid, hasPartRelation, new Date().getTime());
        assertEquals(2, relations.size());
        //assert that B400022028241-RT1/400022028241-14/1795-06-15-01/AdresseContoirsEfterretninger-1795-06-15-01-0011B exists and has an "alto" datastream
        foundPid = getEnhancedFedora().findObjectFromDCIdentifier("path:B400022028241-RT1/400022028241-1/1795-06-13-01/adresseavisen1759-1795-06-13-01-0007B").get(0);
        String altoStream =  getEnhancedFedora().getXMLDatastreamContents(foundPid, "ALTO", new Date().getTime());
        assertTrue(altoStream.length() > 100);
    }
}
