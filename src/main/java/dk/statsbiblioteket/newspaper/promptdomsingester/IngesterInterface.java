package dk.statsbiblioteket.newspaper.promptdomsingester;


import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;

import java.io.File;
import java.security.MessageDigest;

/**
 *
 */
public interface IngesterInterface {

    /**
     * Given a root directory, this method ingests the contents of the root directory to DOMS with the
     * name of the root directory as the label of the root object in DOMS.
     * @param rootDir
     * @return the DOMS pid of the root object.
     */
    String ingest(File rootDir) throws BackendInvalidCredsException, BackendMethodFailedException, PIDGeneratorException, BackendInvalidResourceException, DomsIngesterException;

}
