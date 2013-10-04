package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 10/4/13
 * Time: 2:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleFedoraIngesterTestIntegration extends SimpleFedoraIngesterTest {

    @Override
    EnhancedFedora getEnhancedFedora() throws MalformedURLException, JAXBException, PIDGeneratorException {
        Credentials creds = new Credentials("fedoraAdmin", "fedoraAdminPass");
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, "http://achernar:7880/fedora", "http://achernar:7880/pidgenerator-service" , null);
        return eFedora;
    }

    @Test(groups = "integrationTest")
    @Override
    public void testIngest() throws Exception {
        super.testIngest();
    }
}
