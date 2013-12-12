package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.medieplatform.autonomous.AbstractRunnableComponent;
import dk.statsbiblioteket.medieplatform.autonomous.Batch;
import dk.statsbiblioteket.medieplatform.autonomous.ResultCollector;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import dk.statsbiblioteket.newspaper.promptdomsingester.MultiThreadedFedoraIngester;
import dk.statsbiblioteket.util.Strings;

import java.util.Properties;

public class RunnableMultiThreadedPromptDomsIngester extends AbstractRunnableComponent{

   private final EnhancedFedora eFedora;
    private int concurrency;

    public RunnableMultiThreadedPromptDomsIngester(Properties properties,
                                      EnhancedFedora eFedora,
                                      int concurrency) {
        super(properties);
        this.eFedora = eFedora;
        this.concurrency = concurrency;
    }

    @Override
    public String getEventID() {
        return "Metadata_Archived";
    }

    @Override
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector) {
        TreeIterator iterator = createIterator(batch);
        MultiThreadedFedoraIngester ingester = new MultiThreadedFedoraIngester(eFedora);
        try {
            ingester.ingest(iterator);
        } catch (Exception e) {
            resultCollector.addFailure(
                    batch.getFullID(),
                    "exception",
                    e.getClass().getSimpleName(),
                    "Exception during ingest: " + e.toString(),
                    Strings.getStackTrace(e));
        }
    }
}