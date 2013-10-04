package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;

import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 10/4/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class SimpleFedoraIngesterTest {

    abstract EnhancedFedora getEnhancedFedora() throws MalformedURLException, JAXBException, PIDGeneratorException;

    @Test
    public void testIngest() throws Exception {
        SimpleFedoraIngester ingester = new SimpleFedoraIngester(getEnhancedFedora(), new String[]{".md5"}, new String[]{".xml"}, new String[]{".jp2"}, new String[]{"info:Batch"});
        File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
        File testRoot = new File(rootTestdataDir, "small-test-batch_contents-included/B400022028241-RT1");
        assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
        ingester.ingest(testRoot);
    }
}
