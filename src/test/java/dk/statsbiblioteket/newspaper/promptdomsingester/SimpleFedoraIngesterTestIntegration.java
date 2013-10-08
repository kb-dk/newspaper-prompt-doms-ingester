package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.*;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.newspaper.RecursiveFedoraCleaner;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

/**
 *
 */
public class SimpleFedoraIngesterTestIntegration extends SimpleFedoraIngesterTest {

    @BeforeMethod
    public void setup() throws MalformedURLException, JAXBException, BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException, PIDGeneratorException {
        cleanupFedora();
    }

    @AfterMethod
    public void teardown() throws MalformedURLException, JAXBException, BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException, PIDGeneratorException {
         cleanupFedora();
    }

    public void cleanupFedora() throws MalformedURLException, JAXBException, PIDGeneratorException, BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        String label = "dir:B400022028241-RT1";
        RecursiveFedoraCleaner.cleanFedora(getEnhancedFedora(), label, true);
    }

    @Override
    public EnhancedFedora getEnhancedFedora() throws MalformedURLException, JAXBException, PIDGeneratorException {
        Credentials creds = new Credentials("fedoraAdmin", "fedoraAdminPass");
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, "http://achernar:7880/fedora", "http://achernar:7880/pidgenerator-service" , null);
        return eFedora;
    }

    @Test(groups = "integrationTest")
    @Override
    public void testIngest() throws Exception {
        super.testIngest();
        String pid = super.pid;
        //Now do some clever testing here. e.g. tree-parse doms and see if we get the same structure back.
    }
}
