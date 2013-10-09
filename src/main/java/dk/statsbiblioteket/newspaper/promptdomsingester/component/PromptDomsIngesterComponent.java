package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import com.google.common.base.Throwables;
import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import com.sun.javafx.fxml.PropertyNotFoundException;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.autonomous.AutonomousComponent;
import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.promptdomsingester.IngesterInterface;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Autonomous component for prompt ingest to DOMS.
 * precondition: batch has been uploaded
 * postcondition: on success, issues an event that batch has been ingested in DOMS
 */
public class PromptDomsIngesterComponent implements RunnableComponent {

    private File batchSuperDirectory;
    private EnhancedFedora fedora;
    private static Logger log = LoggerFactory.getLogger(PromptDomsIngesterComponent.class);

    private static String[] requiredProperties = new String[]{
            "fedora.admin.username",
            "fedora.admin.password",
            "doms.server",
            "pidgenerator.location",
            "newspaper.batch.superdir",
            "lockserver",
            "summa"
    }; //etc.

    /**
     * This method reads a properties file either as the first parameter on the command line or as the system
     * variable newspaper.component.properties.file . The following parameters must be defined in the file:
     *            "fedora.admin.username",
     *            "fedora.admin.password",
     *            "doms.server",
     *            "pidgenerator.location",
     *            "newspaper.batch.superdir",
     *            "lockserver",
     *            "summa"
     * @param args
     */
    public static void main(String[] args) throws Exception, JAXBException, PIDGeneratorException {
        log.info("Entered " + PromptDomsIngesterComponent.class);
        Properties properties = readProperties(args);
        Credentials creds = new Credentials(properties.getProperty("fedora.admin.username"), properties.getProperty("fedora.admin.password"));
        String fedoraLocation = properties.getProperty("doms.server");
        EnhancedFedoraImpl eFedora = new EnhancedFedoraImpl(creds, fedoraLocation, properties.getProperty("pidgenerator.location") , null);
        File uploadDir = new File(properties.getProperty("newspaper.batch.superdir"));
        PromptDomsIngesterComponent component = new PromptDomsIngesterComponent(eFedora, uploadDir);
        CuratorFramework lockClient = CuratorFrameworkFactory.newClient(properties.getProperty("lockserver"),
                new ExponentialBackoffRetry(1000, 3));
        lockClient.start();
        BatchEventClient eventClient = createEventClient(properties);
        List<EventID> priorEvents = new ArrayList<EventID>();
        List<EventID> priorEventsExclude =  new ArrayList<EventID>();
        List<EventID> futureEvents = new ArrayList<EventID>();
        priorEvents.add(EventID.Data_Received);
        futureEvents.add(component.getEventID());
        AutonomousComponent autonomous = new AutonomousComponent(component, properties, lockClient, eventClient, 1, priorEvents, priorEventsExclude, futureEvents);
        Map<String, Boolean> result = autonomous.call();
    }

    private static Properties readProperties(String[] args) throws IOException {
        Properties properties = new Properties();
        String propsFileString = null;
        if (args.length >=1) {
            propsFileString = args[0];
        } else {
            propsFileString = System.getProperty("newspaper.component.properties.file");
        }
        if (propsFileString == null) {
            throw new RuntimeException("Properties file must be defined either as command-line parameter or as system" +
                    "property newspaper.component.properties .");
        }
        log.info("Reading properties from " + propsFileString);
        File propsFile = new File(propsFileString);
        if (!propsFile.exists()) {
            throw new FileNotFoundException("No such file: " + propsFile.getAbsolutePath());
        }
        properties.load(new FileReader(propsFile));
        checkProperties(properties, requiredProperties);
        return properties;
    }

    private static BatchEventClient createEventClient(Properties properties) {
        return new BatchEventClientImpl(properties.getProperty("summa"), properties.getProperty("doms.server"),
                                        properties.getProperty("fedora.admin.username"), properties.getProperty("fedora.admin.password"),
                                        properties.getProperty("pidgenerator.location"));
    }


    private static void checkProperties(Properties props, String[] propnames) throws PropertyNotFoundException {
            for (String prop: propnames) {
                if (props.getProperty(prop) == null) {
                    throw new PropertyNotFoundException("Property not found: " + prop);
                }
            }
    }


    /**
     *
     * @param batchSuperDirectory the directory in which batches are found.
     * @param fedora the fedora instance to ingest into.
     */
    public PromptDomsIngesterComponent(EnhancedFedora fedora, File  batchSuperDirectory) {
        this.batchSuperDirectory = batchSuperDirectory;
        this.fedora = fedora;
    }

    @Override
    public String getComponentName() {
         return "Prompt Doms Ingester";
    }

    @Override
    public String getComponentVersion() {
         return "1.0";
    }

    @Override
    public EventID getEventID() {
        return EventID.Data_Archived;
    }

    @Override
    public void doWorkOnBatch(Batch batch, ResultCollector resultCollector) throws Exception {
        Long batchId = batch.getBatchID();
        Integer rt = batch.getRoundTripNumber();
        File rootFile = new File(batchSuperDirectory, batchId + "_RT" + rt);
        if (!rootFile.exists()) {
            resultCollector.addFailure(batchId+"", "No Such File", getComponentName(), "File " + rootFile.getAbsolutePath() + " doesn't exist.");
            resultCollector.setSuccess(false);
            return;
        }
        IngesterInterface ingester = SimpleFedoraIngester.getNewspaperInstance(fedora);
        try {
            ingester.ingest(rootFile);
        } catch (Exception e) {
            resultCollector.addFailure(batchId + "", e.getClass().getName(), getComponentName(), e.getMessage(), Throwables.getStackTraceAsString(e));
            resultCollector.setSuccess(false);
            return;
        }
        resultCollector.setSuccess(true);
    }
}

