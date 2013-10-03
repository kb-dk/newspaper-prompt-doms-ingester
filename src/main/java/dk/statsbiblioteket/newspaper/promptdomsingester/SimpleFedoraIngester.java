package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.io.File;
import java.security.MessageDigest;

/**
 * An implementation of AbstractFedoraIngester where the types of file (data/metadata/checksum) can
 * be determined simply by looking at the file suffixes. (This is probably all that we will ever need,
 * but there is no fundamental problem if we ever need to write alternative implementations that look at
 * the file contents or, say, match a regexp to the filename instead.)
 */
public class SimpleFedoraIngester extends AbstractFedoraIngester {

    private EnhancedFedora fedora;
    private String[] metadataFileSuffixes;
    private String[] dataFileSuffixes;
    private String[] checksumFileSuffixes;

    public SimpleFedoraIngester(EnhancedFedora fedora, String[] checksumFileSuffixes, String[] dataFileSuffixes, String[] metadataFileSuffixes) {
        this.fedora = fedora;
        this.checksumFileSuffixes = checksumFileSuffixes;
        this.dataFileSuffixes = dataFileSuffixes;
        this.metadataFileSuffixes = metadataFileSuffixes;
    }

    @Override
    public EnhancedFedora getEnhancedFedora() {
        return fedora;
    }

    @Override
    public boolean isMetadataFile(File file) {
        //Check if it has a metadata suffix
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isDataFile(File file) {
        //Check if it has a data suffix
        throw new RuntimeException("not implemented");
    }

    @Override
    public boolean isChecksumFile(File file) {
        //Check if it has a checksum file
        throw new RuntimeException("not implemented");
    }

    @Override
    public MessageDigest getKnownChecksum(File file) {
        //TODO: Check through all the checksum files to see if there is one with the checksum for
        //this file.
        return null;
    }
}
