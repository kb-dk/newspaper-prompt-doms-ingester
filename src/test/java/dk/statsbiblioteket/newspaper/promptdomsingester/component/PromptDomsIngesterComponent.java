package dk.statsbiblioteket.newspaper.promptdomsingester.component;

import com.google.common.base.Throwables;
import dk.statsbiblioteket.autonomous.ResultCollector;
import dk.statsbiblioteket.autonomous.RunnableComponent;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.Batch;
import dk.statsbiblioteket.newspaper.processmonitor.datasources.EventID;
import dk.statsbiblioteket.newspaper.promptdomsingester.DomsIngesterException;
import dk.statsbiblioteket.newspaper.promptdomsingester.IngesterInterface;
import dk.statsbiblioteket.newspaper.promptdomsingester.SimpleFedoraIngester;

import java.io.File;

/**
 * Autonomous component for prompt ingest to DOMS.
 * precondition: batch has been uploaded
 * postcondition: on success, issues an event that batch has been ingested in DOMS
 */
public class PromptDomsIngesterComponent implements RunnableComponent {

    private File batchSuperDirectory;
    private EnhancedFedora fedora;
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

