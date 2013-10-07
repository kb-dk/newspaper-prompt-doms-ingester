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

    public SimpleFedoraIngester(EnhancedFedora fedora, String[] dataFileSuffixes, String[] collections) {
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
    public boolean isDataFile(String localname) {
        for (String suffix: dataFileSuffixes) {
            if (localname.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

}
