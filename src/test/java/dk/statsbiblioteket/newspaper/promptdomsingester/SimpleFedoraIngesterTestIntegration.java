package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.*;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
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
import java.util.Properties;

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
    public EnhancedFedora getEnhancedFedora() throws JAXBException, PIDGeneratorException, MalformedURLException {
        Properties props = new Properties();
        try {
            props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Credentials creds = new Credentials(props.getProperty("fedora.admin.username"), props.getProperty("fedora.admin.password"));
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, props.getProperty("fedora.server"), props.getProperty("pidgenerator.location") , null);
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
