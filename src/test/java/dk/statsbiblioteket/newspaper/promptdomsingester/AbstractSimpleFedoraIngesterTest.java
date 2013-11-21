package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.util.Files;
import org.testng.annotations.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

/**
 *
 */
public abstract class AbstractSimpleFedoraIngesterTest {

    String pid;

    protected abstract EnhancedFedora getEnhancedFedora() throws
                                                          MalformedURLException,
                                                          JAXBException,
                                                          PIDGeneratorException;

    /**
     * Test that we can ingest a sample batch. Subclasses of this class should appropriate asserts to test that this
     * test does what it should.
     *
     * @throws Exception
     */
    @Test
    public void testIngest() throws Exception {
        //SimpleFedoraIngester ingester = new SimpleFedoraIngester(getEnhancedFedora(), new String[]{".jp2"}, new String[]{"info:Batch"});
        SimpleFedoraIngester ingester = SimpleFedoraIngester.getNewspaperInstance(getEnhancedFedora());
        File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
        File testSource = new File(rootTestdataDir, "small-test-batch/B400022028241-RT1/");
        File testRoot = new File("/tmp/", "B400022028242-RT1");

        if (testRoot.isDirectory()) {
            Files.delete(testRoot);
        }

        Files.copy(testSource, testRoot, true);

        assertTrue(testRoot.exists(), testRoot.getAbsolutePath() + " does not exist.");
        TransformingIteratorForFileSystems iterator = new TransformingIteratorForFileSystems(
                testRoot, Pattern.quote("."), ".*\\.jp2$", ".md5");

        String rootPid = ingester.ingest(iterator);
        pid = rootPid;
        System.out
              .println("Created object tree rooted at " + rootPid);
        Files.delete(testRoot);
    }
}
