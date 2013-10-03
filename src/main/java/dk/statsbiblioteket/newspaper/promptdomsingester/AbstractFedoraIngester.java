package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.iterator.AbstractIterator;
import dk.statsbiblioteket.doms.iterator.filesystem.IteratorForFileSystems;
import dk.statsbiblioteket.doms.iterator.filesystem.TransformingIteratorForFileSystems;

import java.io.File;

/**
 * Class containing the actual logic for traversing the directory tree and ingesting the data to
 * DOMS. Concrete implementing subclasses need only specify the logic for determining which files are
 * data/metadata/checksums, as well as providing a connection to Fedora.
 */
public abstract class AbstractFedoraIngester implements IngesterInterface {

    public abstract EnhancedFedora getEnhancedFedora();

    @Override
    public String ingest(File rootDir) {
        AbstractIterator<File> iterator = new TransformingIteratorForFileSystems(rootDir, "\\.", ".*\\.jp2");
        while (iterator.hasNext()) {

        }
        throw new RuntimeException("not implemented");

    }

}