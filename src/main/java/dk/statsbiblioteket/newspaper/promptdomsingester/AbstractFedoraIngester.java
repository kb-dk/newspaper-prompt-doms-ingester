package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedoraImpl;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.common.Event;
import dk.statsbiblioteket.doms.iterator.common.EventType;
import dk.statsbiblioteket.doms.iterator.filesystem.IteratorForFileSystems;
import dk.statsbiblioteket.doms.iterator.filesystem.TransformingIteratorForFileSystems;
import dk.statsbiblioteket.doms.webservices.authentication.Credentials;
import org.apache.commons.io.FilenameUtils;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
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

    String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";

    public abstract EnhancedFedora getEnhancedFedora();

    /**
     * Returns a list of collections all new objects must belong to. May be empty.
     * @return
     */
    public abstract List<String> getCollections();

    @Override
    public String ingest(File rootDir) throws BackendInvalidCredsException, BackendMethodFailedException, PIDGeneratorException, BackendInvalidResourceException {
        EnhancedFedora fedora = getEnhancedFedora();
        Deque<String> pidStack = new ArrayDeque<String>();
        AbstractIterator<File> iterator = new TransformingIteratorForFileSystems(rootDir, "\\.", ".*\\.jp2");
        while (iterator.hasNext()) {
            Event event = iterator.next();
            switch (event.getType()) {
                case NodeBegin :
                    String path = event.getPath();
                    String dir = FilenameUtils.getName(path);
                    String id = "dir:" + dir;
                    ArrayList<String> oldIds = new ArrayList<String>();
                    oldIds.add(id);
                    String pid = fedora.newEmptyObject(oldIds, getCollections(), "Created object representing " + path);
                    String parentPid = pidStack.peekFirst();
                    pidStack.addFirst(pid);
                    if (parentPid != null) {
                        fedora.addRelation(parentPid, null, hasPartRelation, pid, false, parentPid + " hasPart " + pid);
                    }
                    break;
                case NodeEnd:
                    pidStack.removeFirst();
                    break;
                case Attribute:
                    break;
            }
        }
        throw new RuntimeException("not implemented");

    }

}