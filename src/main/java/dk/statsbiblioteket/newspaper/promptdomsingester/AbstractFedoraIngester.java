package dk.statsbiblioteket.newspaper.promptdomsingester;

import com.google.common.io.CharStreams;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.AttributeParsingEvent;
import dk.statsbiblioteket.doms.iterator.common.ParsingEvent;
import dk.statsbiblioteket.doms.iterator.filesystem.transforming.TransformingIteratorForFileSystems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Class containing the actual logic for traversing the directory tree and ingesting the data to
 * DOMS. Concrete implementing subclasses need only specify the logic for determining which files are
 * data/metadata/checksums, as well as providing a connection to Fedora.
 */
public abstract class AbstractFedoraIngester implements IngesterInterface {

    private Logger log = LoggerFactory.getLogger(getClass());

    String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    String hasFileRelation = "info:fedora/fedora-system:def/relations-external#hasFile";


    public abstract EnhancedFedora getEnhancedFedora();

    /**
     * Returns a list of collections all new objects must belong to. May be empty.
     * @return
     */
    public abstract List<String> getCollections();

    @Override
    public String ingest(File rootDir) throws BackendInvalidCredsException, BackendMethodFailedException, PIDGeneratorException, BackendInvalidResourceException, DomsIngesterException {
        EnhancedFedora fedora = getEnhancedFedora();
        Deque<String> pidStack = new ArrayDeque<String>();
        Deque<String> labelStack = new ArrayDeque<String>();
        AbstractIterator<File> iterator = new TransformingIteratorForFileSystems(rootDir, "\\.", ".*\\.jp2", ".md5");
        String rootPid = null;
        while (iterator.hasNext()) {
            ParsingEvent event = iterator.next();
            switch (event.getType()) {
                case NodeBegin :
                    String dir = event.getLocalname();
                    labelStack.addFirst(dir);
                    String id = "path:" + getPath(labelStack);
                    ArrayList<String> oldIds = new ArrayList<String>();
                    oldIds.add(id);
                    String logMessage = "Created object with DC id " + id;
                    String currentNodePid = fedora.newEmptyObject(oldIds, getCollections(), logMessage);
                    log.debug(logMessage + " / " + currentNodePid);
                    String parentPid = pidStack.peekFirst();
                    if (rootPid == null) {
                        rootPid = currentNodePid;
                    }
                    pidStack.addFirst(currentNodePid);
                    if (parentPid != null) {
                        String comment = "Added relationship " + parentPid + " hasPart " + currentNodePid;
                        fedora.addRelation(parentPid, null, hasPartRelation, currentNodePid, false, comment);
                        log.debug(comment);
                    }
                    if (isDataFile(dir)) {
                        String comment = "Added relationship " + parentPid + " hasFile " + currentNodePid;
                        fedora.addRelation(parentPid, null, hasFileRelation, currentNodePid, false, comment);
                        log.debug(comment);
                    }
                    break;
                case NodeEnd:
                    pidStack.removeFirst();
                    labelStack.removeFirst();
                    break;
                case Attribute:
                    AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
                    if (event.getLocalname().equals("contents")) {
                        log.debug("Skipping contents attribute.");
                    } else {
                        String comment = "Adding datastream for " + attributeParsingEvent.getLocalname() + " to " + getPath(labelStack) + " == " + labelStack.peekFirst();
                        log.debug(comment);
                        String[] splitName = attributeParsingEvent.getLocalname().split("\\.");
                        if (splitName.length < 2) {
                            throw new DomsIngesterException("Cannot find datastream name in " + attributeParsingEvent.getLocalname());
                        }
                        String datastreamName = splitName[splitName.length - 2];
                        log.debug("Ingesting datastream '" + datastreamName + "'");
                        String metadataText = null;
                        try {
                            metadataText = CharStreams.toString(new InputStreamReader(attributeParsingEvent.getText(), "UTF-8"));
                        } catch (IOException e) {
                            throw new DomsIngesterException(e);
                        }
                        String checksum = null;
                        try {
                            checksum = attributeParsingEvent.getChecksum();
                            checksum = checksum.toLowerCase();
                        } catch (IOException e) {
                            throw new DomsIngesterException(e);
                        }
                        if (checksum != null) {
                            fedora.modifyDatastreamByValue(pidStack.peekFirst(), datastreamName, metadataText, checksum, "Added by ingester.");
                        } else {
                            fedora.modifyDatastreamByValue(pidStack.peekFirst(), datastreamName, metadataText, "Added by ingester.");

                        }
                    }
                    break;
            }
        }
        return rootPid;
    }

    /**
     * Returns the path to where we are now with "/" as separator.
     * @param path
     * @return
     */
    private String getPath(Deque<String> path) {
        String result = "";
        for (String dir: path) {
            result = dir + "/" + result;
        }
        if (result.length() > 0) {
            result = result.substring(0, result.length()-1);
        }
        return result;
    }

}