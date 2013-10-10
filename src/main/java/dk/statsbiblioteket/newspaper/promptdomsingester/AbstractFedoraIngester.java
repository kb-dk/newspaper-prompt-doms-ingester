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
 * data/checksums, as well as providing a connection to Fedora.
 */
public abstract class AbstractFedoraIngester
        implements IngesterInterface {

    String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    String hasFileRelation = "http://doms.statsbiblioteket.dk/relations/default/0/1/#hasFile";
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Get an EnhancedFedora object for the repository in which ingest is required.
     * @return the enhanced fedora.
     */
    protected abstract EnhancedFedora getEnhancedFedora();

    /**
     * Returns a regexp which can identify data files in this collection, e.g. ".*\\.jp2"
     * @return the regexp as a String
     */
    protected abstract String getDataFilePattern();

    /**
     * Returns the postfix for checksum files in this collection. e.g. ".md5"
     * @return the checksum postfix.
     */
    protected abstract String getChecksumPostfix();

    /**
     * Returns a list of collections all new objects must belong to. May be empty.
     * @return
     */
    protected abstract List<String> getCollections();

    /**
     * The logic of this method it that it maintains two stacks in parallel to tell it exactly where it is in the
     * directory hierarchy. pathElementStack is simply a stack of directory names and pidStack is a stack of the
     * corresponding DOMS pids. These stacks are pushed for each NodeBegin event and popped for each NodeEnd event.
     * Thus Attributes (ie metadata files) are always ingested as datastreams to the object currently at the top of the
     * stack. The pidStack tells us which object to modify, the pathElementStack tells us how to label the modifications.
     *
     * @param rootDir the root dir to parse from
     *
     * @return the doms pid of the root object created
     * @throws DomsIngesterException if failing to read a file or any file is encountered without a checksum
     */
    @Override
    public String ingest(File rootDir)
            throws
            BackendInvalidCredsException,
            BackendMethodFailedException,
            PIDGeneratorException,
            BackendInvalidResourceException,
            DomsIngesterException {
        EnhancedFedora fedora = getEnhancedFedora();
        Deque<String> pidStack = new ArrayDeque<String>();
        Deque<String> pathElementStack = new ArrayDeque<String>();
        AbstractIterator<File> iterator = new TransformingIteratorForFileSystems(rootDir, "\\.", getDataFilePattern(), getChecksumPostfix());
        String rootPid = null;
        while (iterator.hasNext()) {
            ParsingEvent event = iterator.next();
            switch (event.getType()) {
                case NodeBegin:
                    String dir = event.getLocalname();
                    pathElementStack.addFirst(dir);
                    String id = "path:" + getPath(pathElementStack);
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
                    if (dir.matches(getDataFilePattern())) {
                        String comment = "Added relationship " + parentPid + " hasFile " + currentNodePid;
                        fedora.addRelation(parentPid, null, hasFileRelation, currentNodePid, false, comment);
                        log.debug(comment);
                    }
                    break;
                case NodeEnd:
                    pidStack.removeFirst();
                    pathElementStack.removeFirst();
                    //Possible publish of object here?
                    break;
                case Attribute:
                    AttributeParsingEvent attributeParsingEvent = (AttributeParsingEvent) event;
                    if (event.getLocalname().equals("contents")) {
                        //Possibly check that you are in a DataFileDir before ignoring the event?
                        log.debug("Skipping contents attribute.");
                    } else {
                        String directoryPath = getPath(pathElementStack);
                        String comment =
                                "Adding datastream for " + attributeParsingEvent.getLocalname() + " to " + directoryPath
                                + " == " + pidStack.peekFirst();
                        String filePath = directoryPath + "/" + event.getLocalname();
                        List<String> alternativeIdentifiers = new ArrayList<>();
                        alternativeIdentifiers.add(filePath);
                        log.debug(comment);
                        String datastreamName = getDatastreamName(attributeParsingEvent.getLocalname());
                        log.debug("Ingesting datastream '" + datastreamName + "'");
                        String metadataText;
                        try {
                            metadataText = CharStreams
                                    .toString(new InputStreamReader(attributeParsingEvent.getText(), "UTF-8"));
                        } catch (IOException e) {
                            throw new DomsIngesterException(e);
                        }
                        String checksum = null;
                        try {
                            checksum = attributeParsingEvent.getChecksum().toLowerCase();
                        } catch (IOException e) {
                            throw new DomsIngesterException(e);
                        }
                        if (checksum != null) {
                            fedora.modifyDatastreamByValue(pidStack.peekFirst(),
                                                           datastreamName,
                                                           metadataText,
                                                           checksum,
                                                           alternativeIdentifiers,
                                                           "Added by ingester.");
                        } else {
                            fedora.modifyDatastreamByValue(pidStack.peekFirst(),
                                                           datastreamName,
                                                           metadataText,
                                                           alternativeIdentifiers,
                                                           "Added by ingester.");

                        }
                    }
                    break;
            }
        }
        return rootPid;
    }

    private static String getDatastreamName(String attributeName)
            throws
            DomsIngesterException {
        String[] splitName = attributeName.split("\\.");
        if (splitName.length < 2) {
            throw new DomsIngesterException(
                    "Cannot find datastream name in " + attributeName);
        }
        return splitName[splitName.length - 2].toUpperCase();
    }

    /**
     * Returns the path to where we are now with "/" as separator.
     *
     * @param path
     *
     * @return
     */
    private String getPath(Deque<String> path) {
        String result = "";
        for (String dir : path) {
            result = dir + "/" + result;
        }
        if (result.length() > 0) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

}