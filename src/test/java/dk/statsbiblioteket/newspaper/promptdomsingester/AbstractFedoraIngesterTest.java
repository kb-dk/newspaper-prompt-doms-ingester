package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.newspaper.TestConstants;
import dk.statsbiblioteket.util.Files;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.util.regex.Pattern;

import static org.testng.Assert.assertTrue;

/**
 *
 */
public abstract class AbstractFedoraIngesterTest {

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
    public void testIngest(IngesterInterface ingester) throws Exception {

        File rootTestdataDir = new File(System.getProperty("integration.test.newspaper.testdata"));
        File testSource = new File(rootTestdataDir, "small-test-batch/B400022028241-RT1/");
        File testRoot = new File(System.getProperty("user.dir")+"/target/temp/", TestConstants.TEST_BATCH_ID);

        if (testRoot.isDirectory()) {
            Files.delete(testRoot);
        }
        testRoot.deleteOnExit();

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