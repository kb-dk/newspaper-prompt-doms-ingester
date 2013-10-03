package dk.statsbiblioteket.newspaper.promptdomsingester;


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
    String ingest(File rootDir);

    boolean isMetadataFile(File file);

    boolean isDataFile(File file);

    boolean isChecksumFile(File file);

    /**
     * If a file has an associated checksum then return it in the form of a MessageDigest. Otherwise
     * return null.
     * @param file The file for which the checksum is required
     * @return the checksum, or null if no checksum is known.
     */
    MessageDigest getKnownChecksum(File file);

}
