package dk.statsbiblioteket.newspaper.promptdomsingester;

import com.google.common.io.CharStreams;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.NodeBeginsParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.ParsingEvent;
import dk.statsbiblioteket.medieplatform.autonomous.iterator.common.TreeIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

public class MultiThreadedFedoraIngester extends RecursiveTask<String> implements IngesterInterface {

    private static final String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    private final EnhancedFedora fedora;
    private String[] collections;
    private List<ForkJoinTask<String>> childPids = new ArrayList<>();
    private TreeIterator iterator;
    private Logger log = LoggerFactory.getLogger(getClass());
    private String myPid;

    public MultiThreadedFedoraIngester(EnhancedFedora fedora, String[] collections) {
        this.fedora = fedora;
        this.collections = collections;
    }

    protected MultiThreadedFedoraIngester(EnhancedFedora fedora, TreeIterator iterator,String[] collections) {
        this(fedora,collections);
        this.iterator = iterator;
    }

    private static String getDatastreamName(String attributeName) throws DomsIngesterException {
        String[] splitName = attributeName.split("\\.");
        if (splitName.length < 2) {
            throw new DomsIngesterException("Cannot find datastream name in " + attributeName);
        }
        return splitName[splitName.length - 2].toUpperCase();
    }

    @Override
    protected String compute() {
        boolean firstEvent = true;
        try {
        while (iterator.hasNext()) {
            ParsingEvent event = iterator.next();
            switch (event.getType()) {
                case NodeBegin:
                    if (firstEvent) {
                        myPid = handleNodeBegin((NodeBeginsParsingEvent) event);
                        firstEvent = false;
                    } else {
                        TreeIterator childIterator = iterator.skipToNextSibling();
                        MultiThreadedFedoraIngester childIngester = new MultiThreadedFedoraIngester(
                                fedora, childIterator,collections);
                        childPids.add(childIngester.fork());
                    }
                    break;
                case Attribute:
                    handleAttribute((AttributeParsingEvent) event);
                    break;
                case NodeEnd:
                    handleNodeEnd();
                    break;
            }
        }
        } catch (Exception e){
            throw new RuntimeException(e);
        }

        return myPid;
    }

    private String handleNodeBegin(NodeBeginsParsingEvent event) throws
                                                                 BackendInvalidCredsException,
                                                                 BackendMethodFailedException,
                                                                 PIDGeneratorException,
                                                                 BackendInvalidResourceException {
        String id = getDCidentifier(event);
        String currentNodePid = exists(fedora, event);
        if (currentNodePid == null) {
            ArrayList<String> oldIds = new ArrayList<>();
            oldIds.add(id);
            String logMessage = "Created object with DC id " + id;
            currentNodePid = fedora.newEmptyObject(oldIds, getCollections(), logMessage);
            log.debug("{}" + logMessage + " / " + currentNodePid, currentNodePid);
        }
        return currentNodePid;
    }

    private void handleNodeEnd() throws
                                 BackendMethodFailedException,
                                 BackendInvalidResourceException,
                                 BackendInvalidCredsException {
        ArrayList<String> childRealPids = new ArrayList<>();
        for (ForkJoinTask<String> childPid : childPids) {
            childRealPids.add(childPid.join());
        }
        String comment
                = "Added relationship " + myPid + " hasPart '" + Arrays.deepToString(childRealPids.toArray()) + "'";
        fedora.addRelations(myPid, null, hasPartRelation, childRealPids, false, comment);
        log.debug("{}, " + comment, myPid);

    }

    private void handleAttribute(AttributeParsingEvent event) throws
                                                              DomsIngesterException,
                                                              BackendInvalidCredsException,
                                                              BackendMethodFailedException,
                                                              BackendInvalidResourceException {
        if (event.getName().endsWith("/contents")) {
            //Possibly check that you are in a DataFileDir before ignoring the myObjectEvent?
            log.debug("{}, Skipping contents attribute.", myPid);
        } else {
            String comment = "Adding datastream for " + event.getName() + " == " + myPid;
            List<String> alternativeIdentifiers = new ArrayList<>();
            alternativeIdentifiers.add(event.getName());
            log.debug("{}," + comment, myPid);
            String datastreamName = getDatastreamName(event.getName());
            log.debug("{}, Ingesting datastream '" + datastreamName + "'", myPid);
            String metadataText;
            try {
                metadataText = CharStreams.toString(new InputStreamReader(event.getData(), "UTF-8"));
            } catch (IOException e) {
                throw new DomsIngesterException(e);
            }
            String checksum = null;
            try {
                checksum = event.getChecksum().toLowerCase();
            } catch (IOException e) {
                throw new DomsIngesterException(e);
            }
            if (checksum != null) {
                fedora.modifyDatastreamByValue(
                        myPid, datastreamName, metadataText, checksum, alternativeIdentifiers, "Added by ingester.");
            } else {
                fedora.modifyDatastreamByValue(
                        myPid, datastreamName, metadataText, alternativeIdentifiers, "Added by ingester.");

            }
        }
    }

    private String exists(EnhancedFedora fedora, NodeBeginsParsingEvent nodeBeginsParsingEvent) throws
                                                                                                BackendInvalidCredsException,
                                                                                                BackendMethodFailedException {
        List<String> founds = fedora.findObjectFromDCIdentifier(getDCidentifier(nodeBeginsParsingEvent));
        if (founds != null && founds.size() > 0) {
            return founds.get(0);
        } else {
            return null;
        }
    }

    private String getDCidentifier(NodeBeginsParsingEvent event) {
        String dir = event.getName();
        return "path:" + dir;
    }

    /**
     * Returns a list of collections all new objects must belong to. May be empty.
     *
     * @return
     */
    protected List<String> getCollections() {
        return Arrays.asList(collections);
    }

    @Override
    public String ingest(TreeIterator iterator) {
        this.iterator = iterator;
        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        ForkJoinTask<String> result;
        result = forkJoinPool.submit(this);
        forkJoinPool.shutdown();
        try {
            forkJoinPool.awaitTermination(1, TimeUnit.DAYS);
        } catch (InterruptedException e) {
            //TODO
        }
        if (result != null) {
            return result.join();
        } else {
            return null;
        }
    }
}
