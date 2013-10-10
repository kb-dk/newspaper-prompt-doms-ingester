package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.promptdomsingester.IngesterInterface;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;

import java.io.File;

/**
 * The runnable component for the PromptDomsIngester
 */
public class RunnablePromptDomsIngester implements RunnableComponent{


    private final EnhancedFedora eFedora;
    private final File uploadDir;

    public RunnablePromptDomsIngester(EnhancedFedora eFedora,
                                      File uploadDir) {
        //To change body of created methods use File | Settings | File Templates.
        this.eFedora = eFedora;
        this.uploadDir = uploadDir;
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
    public void doWorkOnBatch(Batch batch,
                              ResultCollector resultCollector)
            throws
            Exception {

        File rootFile = new File(uploadDir, batch.getFullID());
        if (!rootFile.exists()) {
            resultCollector.addFailure(batch.getFullID(),
                                       "No Such File",
                                       getComponentName(),
                                       "File " + rootFile.getAbsolutePath() + " doesn't exist.");
            resultCollector.setSuccess(false);
            return;
        }
        IngesterInterface ingester = SimpleFedoraIngester.getNewspaperInstance(eFedora);
        try {
            ingester.ingest(rootFile);
        } catch (Exception e) {
            resultCollector.addFailure(batch.getFullID(),
                                       e.getClass().getName(),
                                       getComponentName(),
                                       e.getMessage(),
                                       Throwables.getStackTraceAsString(e));
            resultCollector.setSuccess(false);
            return;
        }
        resultCollector.setSuccess(true);
    }

}
