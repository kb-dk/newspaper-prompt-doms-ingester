package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private String[] collections;

    public SimpleFedoraIngester(EnhancedFedora fedora, String[] checksumFileSuffixes, String[] dataFileSuffixes, String[] metadataFileSuffixes, String[] collections) {
        this.fedora = fedora;
        this.checksumFileSuffixes = checksumFileSuffixes;
        this.dataFileSuffixes = dataFileSuffixes;
        this.metadataFileSuffixes = metadataFileSuffixes;
        this.collections = collections;
    }

    @Override
    public EnhancedFedora getEnhancedFedora() {
        return fedora;
    }

    @Override
    public List<String> getCollections() {
        if (collections == null) {
            collections = new String[]{};
        }
        return new ArrayList<String>(Arrays.asList(collections));
    }

    @Override
    public boolean isMetadataFile(File file) {
        for (String suffix: metadataFileSuffixes) {
            if (file.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDataFile(File file) {
        for (String suffix: dataFileSuffixes) {
            if (file.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isChecksumFile(File file) {
        for (String suffix: checksumFileSuffixes) {
            if (file.getName().endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public MessageDigest getKnownChecksum(File file) {
        //TODO: Check through all the checksum files to see if there is one with the checksum for
        //this file.
        return null;
    }
}
