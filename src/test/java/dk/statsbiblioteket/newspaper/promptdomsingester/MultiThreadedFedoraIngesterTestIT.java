package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.newspaper.RecursiveFedoraCleaner;
import dk.statsbiblioteket.newspaper.TestConstants;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MultiThreadedFedoraIngesterTestIT extends AbstractFedoraIngesterTest {

    private final Logger log = LoggerFactory.getLogger(getClass());

    String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";


    @BeforeMethod
    public void setup() throws
                        MalformedURLException,
                        JAXBException,
                        BackendInvalidCredsException,
                        BackendMethodFailedException,
                        BackendInvalidResourceException,
                        PIDGeneratorException {
        cleanupFedora();
    }

    @AfterMethod
    public void teardown() throws
                           MalformedURLException,
                           JAXBException,
                           BackendInvalidCredsException,
                           BackendMethodFailedException,
                           BackendInvalidResourceException,
                           PIDGeneratorException {
        cleanupFedora();
    }

    public void cleanupFedora() throws
                                MalformedURLException,
                                JAXBException,
                                PIDGeneratorException,
                                BackendInvalidCredsException,
                                BackendMethodFailedException,
                                BackendInvalidResourceException {
        String label = TestConstants.TEST_BATCH_PATH;
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
        Credentials creds = new Credentials(
                props.getProperty(ConfigConstants.DOMS_USERNAME),
                props.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = props.getProperty(ConfigConstants.DOMS_URL);
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(
                creds,
                fedoraLocation,
                props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL),
                null);
        return eFedora;
    }

    @Test(groups = "integrationTest")
    public void testIngest() throws Exception {
        super.testIngest(new MultiThreadedFedoraIngester(getEnhancedFedora(), new String[0], 8, 2));
        String pid = super.pid;
        String foundPid = getEnhancedFedora().findObjectFromDCIdentifier(TestConstants.TEST_BATCH_PATH).get(0);
        assertEquals(pid, foundPid);
        String nextPid
                = getEnhancedFedora().findObjectFromDCIdentifier(TestConstants.TEST_BATCH_PATH + "/400022028241-1")
                                     .get(0);
        List<FedoraRelation> relations = getEnhancedFedora().getNamedRelations(
                pid,
                hasPartRelation,
                new Date().getTime());
        assertEquals(2, relations.size());
        foundPid
                = getEnhancedFedora().findObjectFromDCIdentifier(TestConstants.TEST_BATCH_PATH + "/400022028241-1/1795-06-01/adresseavisen1759-1795-06-01-0007B")
                                     .get(0);
        String altoStream = getEnhancedFedora().getXMLDatastreamContents(foundPid, "ALTO", new Date().getTime());
        assertTrue(altoStream.length() > 100);
    }

    /**
     * Tests that if we ingest the same batch twice we don't duplicate rdf entries.
     * @throws Exception
     */
    @Test(groups = "integrationTest")
    public void testDoubleIngest() throws Exception {
        log.debug("Doing first ingest.");
        super.testIngest(new MultiThreadedFedoraIngester(getEnhancedFedora(), new String[0], 8, 2));
        log.debug("---------------------------------------------------------------------");
        log.debug("Doing second ingest.");
        super.testIngest(new MultiThreadedFedoraIngester(getEnhancedFedora(), new String[0], 8, 2));
        String pid = super.pid;
        String foundPid = getEnhancedFedora().findObjectFromDCIdentifier(TestConstants.TEST_BATCH_PATH).get(0);
        assertEquals(pid, foundPid);
        String nextPid
                = getEnhancedFedora().findObjectFromDCIdentifier(TestConstants.TEST_BATCH_PATH + "/400022028241-1")
                .get(0);
        List<FedoraRelation> relations = getEnhancedFedora().getNamedRelations(
                pid,
                hasPartRelation,
                new Date().getTime());
        assertEquals(2, relations.size());
        foundPid
                = getEnhancedFedora().findObjectFromDCIdentifier(TestConstants.TEST_BATCH_PATH + "/400022028241-1/1795-06-01/adresseavisen1759-1795-06-01-0007B")
                .get(0);
        String xmlRdf = getEnhancedFedora().getXMLDatastreamContents(pid, "RELS-EXT");
        int rdfMatches = StringUtils.countMatches(xmlRdf, "hasPart");
        int distinctMatches = relations.size();
        /* TODO
        This is a slightly ugly test because it assumes each distinct hasPart
        is represented in xml as
        <hasPart></hasPart>
        but what if it comes out as
        <hasPart/> ?
        This should be fixed up using RDFManipulator to count the number of
        relations properly with xml.
         */
        assertEquals(rdfMatches, 2*distinctMatches);
        assertTrue(rdfMatches > 0, "Should be at least one hasPart relation.");
    }


    /**
     * Tests that if we ingest the same batch twice we don't duplicate rdf entries.
     *
     * @throws Exception
     */
    @Test(groups = "integrationTest")
    public void testDisruptedIngest() throws Exception {
        log.debug("Doing disrupted ingest.");
        Thread threads = new Thread(new Runnable() {
            @Override
            public void run() {
                EnhancedFedora fedora;
                try {
                    fedora = getEnhancedFedora();
                } catch (JAXBException | PIDGeneratorException | MalformedURLException e) {
                    throw new RuntimeException("Fedora Failure", e);
                }
                try {
                    testIngest(new MultiThreadedFedoraIngester(fedora, new String[0], 2, 2));
                    throw new RuntimeException("Was not stoppped");
                } catch (IOException e) {
                    throw new RuntimeException("IO exception", e);
                }
            }
        });
        threads.start();
        Thread.sleep(4000);
        assertEquals(threads.getState(), Thread.State.WAITING);
        System.out.println("Stopping disrupted ingest");
        threads.interrupt();
        Thread.sleep(100);
        System.out.println("Ingest disrupted");
        assertEquals(threads.getState(), Thread.State.TIMED_WAITING);
        threads.join();
        assertEquals(threads.getState(), Thread.State.TERMINATED);
        System.out.println("Waiting for the disrupted ingest to die");
    }
}
