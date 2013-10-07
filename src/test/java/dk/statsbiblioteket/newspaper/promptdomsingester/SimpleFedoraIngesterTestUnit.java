package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 10/4/13
 * Time: 2:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleFedoraIngesterTestUnit extends SimpleFedoraIngesterTest {

    TestEnhancedFedoraImpl fedora = null;

    @Override
    EnhancedFedora getEnhancedFedora() throws MalformedURLException, JAXBException, PIDGeneratorException {
        List<String> datastreamNames = new ArrayList<>();
        datastreamNames.add("mods");
        datastreamNames.add("film");
        datastreamNames.add("edition");
        datastreamNames.add("alto");
        datastreamNames.add("mix");
        this.fedora = new TestEnhancedFedoraImpl(datastreamNames);
        return fedora;
    }

    @Test
    @Override
    public void testIngest() throws Exception {
        super.testIngest();
        System.out.println("Created " + fedora.objectsCreated + " objects.");
        System.out.println("Modified " + fedora.datastreamsModified + " datastreams.");
        System.out.println(fedora.toString());
    }
}
