package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import com.netflix.curator.framework.CuratorFramework;
import com.netflix.curator.framework.CuratorFrameworkFactory;
import com.netflix.curator.retry.ExponentialBackoffRetry;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClient;
import dk.statsbibliokeket.newspaper.batcheventFramework.BatchEventClientImpl;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import dk.statsbiblioteket.medieplatform.autonomous.AutonomousComponent;
import dk.statsbiblioteket.medieplatform.autonomous.CallResult;
import dk.statsbiblioteket.medieplatform.autonomous.ConfigConstants;
import dk.statsbiblioteket.medieplatform.autonomous.RunnableComponent;
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
 * Autonomous component for prompt ingest to DOMS. precondition: batch has been uploaded postcondition: on success,
 * issues an event that batch has been ingested in DOMS
 */
public class PromptDomsIngesterComponent {

    private static Logger log = LoggerFactory.getLogger(PromptDomsIngesterComponent.class);
    private static String[] requiredProperties =
            new String[]{ConfigConstants.DOMS_USERNAME, ConfigConstants.DOMS_PASSWORD, ConfigConstants.DOMS_URL, ConfigConstants.DOMS_PIDGENERATOR_URL,
                         ConfigConstants.AUTONOMOUS_MAXTHREADS, ConfigConstants.AUTONOMOUS_LOCKSERVER_URL, ConfigConstants.AUTONOMOUS_SBOI_URL}; //etc.

    /**
     * This method reads a properties file either as the first parameter on the command line or as the system variable
     * newspaper.component.properties.file .
     *
     * @param args an array of length 1, where the first entry is a path to the properties file
     */
    public static void main(String[] args)
            throws
            Exception,
            JAXBException,
            PIDGeneratorException {
        log.info("Entered " + PromptDomsIngesterComponent.class);
        Properties properties = readProperties(args);
        Credentials creds = new Credentials(properties.getProperty(ConfigConstants.DOMS_USERNAME),
                                            properties.getProperty(ConfigConstants.DOMS_PASSWORD));
        String fedoraLocation = properties.getProperty(ConfigConstants.DOMS_URL);
        EnhancedFedoraImpl eFedora =
                new EnhancedFedoraImpl(creds, fedoraLocation, properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL), null);
        File uploadDir = new File(properties.getProperty(ConfigConstants.ITERATOR_FILESYSTEM_BATCHES_FOLDER));
        RunnableComponent component = new RunnablePromptDomsIngester(properties,eFedora);
        CuratorFramework lockClient = CuratorFrameworkFactory
                .newClient(properties.getProperty(ConfigConstants.AUTONOMOUS_LOCKSERVER_URL), new ExponentialBackoffRetry(1000, 3));
        lockClient.start();
        BatchEventClient eventClient = createEventClient(properties);
        List<String> priorEvents = new ArrayList<>();
        List<String> priorEventsExclude = new ArrayList<>();
        List<String> futureEvents = new ArrayList<>();
        String pastEventsProperty = properties.getProperty(ConfigConstants.AUTONOMOUS_PAST_SUCCESSFUL_EVENTS);
        if (pastEventsProperty != null) {
            for (String property: pastEventsProperty.split(",")) {
                priorEvents.add(property);
            }
        }
        futureEvents.add(component.getEventID());
        int maxThreads = Integer.parseInt(properties.getProperty(ConfigConstants.AUTONOMOUS_MAXTHREADS, "1"));
        AutonomousComponent autonomous = new AutonomousComponent(component,
                                                                 lockClient,
                                                                 eventClient,
                                                                 maxThreads,
                                                                 priorEvents,
                                                                 priorEventsExclude,
                                                                 futureEvents);
        CallResult result = autonomous.call();
        System.out.println(result);
        System.exit(result.containsFailures());
    }

    /**
     * Reads the properties from the arguments or system properties. Either the first argument must be a path to a
     * properties file, or, if not, the system property "newspaper.component.properties.file" must denote such a path.
     * If neither, then a runtime exception is set
     *
     * @param args the command line arguments
     *
     * @return a properties object parsed from the properties file
     * @throws IOException if the file could not be read
     * @throws RuntimeException if no path could be determined
     */
    private static Properties readProperties(String[] args)
            throws
            IOException,
            RuntimeException {
        Properties properties = new Properties();
        String propsFileString;
        if (args.length >= 1) {
            propsFileString = args[0];
        } else {
            propsFileString = System.getProperty("newspaper.component.properties.file");
        }
        if (propsFileString == null) {
            throw new RuntimeException("Properties file must be defined either as command-line parameter or as system"
                                       + "property newspaper.component.properties .");
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

    /**
     * Create a batch event client from the properties
     * @param properties the properties
     * @return an initialised batch event client
     */
    private static BatchEventClient createEventClient(Properties properties) {
        return new BatchEventClientImpl(properties.getProperty(ConfigConstants.AUTONOMOUS_SBOI_URL),
                                        properties.getProperty(ConfigConstants.DOMS_URL),
                                        properties.getProperty(ConfigConstants.DOMS_USERNAME),
                                        properties.getProperty(ConfigConstants.DOMS_PASSWORD),
                                        properties.getProperty(ConfigConstants.DOMS_PIDGENERATOR_URL));
    }

    /**
     * Check that all the required properties are set
     * @param props the properties to check
     * @param propnames the names that must be found in the properties
     * @throws RuntimeException if any name could not be found
     */
    private static void checkProperties(Properties props,
                                        String[] propnames)
            throws
            RuntimeException {
        for (String prop : propnames) {
            if (props.getProperty(prop) == null) {
                throw new RuntimeException("Property not found: " + prop);
            }
        }
    }
}

