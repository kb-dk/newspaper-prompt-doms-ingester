package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.newspaper.promptdomsingester.IngesterInterface;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;

import java.util.Properties;

/**
 * The runnable component for the PromptDomsIngester
 */
public class RunnablePromptDomsIngester extends AbstractRunnableComponent{


    private final EnhancedFedora eFedora;

    public RunnablePromptDomsIngester(Properties properties,
                                      EnhancedFedora eFedora) {
        super(properties);
        this.eFedora = eFedora;
    }

    @Override
    public String getComponentName() {
        return "Prompt Doms Ingester";
    }

    @Override
    public String getComponentVersion() {
        return getClass().getPackage().getImplementationVersion();
    }

    @Override
    public String getEventID() {
        return "Data_Archived";
    }

    @Override
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {
        IngesterInterface ingester = SimpleFedoraIngester.getNewspaperInstance(eFedora);
        try {
            ingester.ingest(createIterator(batch));
        } catch (Exception e) {
            resultCollector.addFailure(batch.getFullID(),
                                       e.getClass().getName(),
                                       getComponentName(),
                                       e.getMessage(),
                                       Throwables.getStackTraceAsString(e));
            return;
        }
    }

}
