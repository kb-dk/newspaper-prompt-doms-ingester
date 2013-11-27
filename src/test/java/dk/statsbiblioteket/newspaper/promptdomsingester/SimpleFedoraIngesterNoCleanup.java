package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 10/9/13
 * Time: 1:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class SimpleFedoraIngesterNoCleanup extends AbstractSimpleFedoraIngesterTest {

    @Override
    public EnhancedFedora getEnhancedFedora() throws JAXBException, PIDGeneratorException, MalformedURLException {
        Properties props = new Properties();
        try {
            props.load(new FileReader(new File(System.getProperty("integration.test.newspaper.properties"))));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Credentials creds = new Credentials(props.getProperty(ConfigConstants.DOMS_USERNAME), props.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = props.getProperty(ConfigConstants.DOMS_URL);
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, fedoraLocation, props.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL) , null);
        return eFedora;
    }


    public void testIngest() throws Exception {
        //SimpleFedoraIngester ingester = new SimpleFedoraIngester(getEnhancedFedora(), new String[]{".jp2"}, new String[]{"info:Batch"});
        SimpleFedoraIngester ingester = SimpleFedoraIngester.getNewspaperInstance(getEnhancedFedora());
        File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
        File testRoot = new File(rootTestdataDir, "small-test-batch/B400022028241-RT1");
        assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
        TransformingIteratorForFileSystems iterator =
                new TransformingIteratorForFileSystems(testRoot, Pattern.quote("."), ".*\\.jp2$", ".md5");
        String rootPid = ingester.ingest(iterator);
        pid = rootPid;
        System.out.println("Created object tree rooted at " + rootPid);
    }

}
