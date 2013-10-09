package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;

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
    private String[] checksumFileSuffixes;
    private String[] collections;

    private String dataFilePattern;
    private String checksumPostfix;

    /**
     * Constructor for this method.
     * @param fedora the fedora instance in which to ingest.
     * @param dataFilePattern the the pattern (java regexp) by which one can identify data files in the collection to be
     *                        ingested.
     * @param collections the DOMS collections in which to ingest objects.
     */
    public SimpleFedoraIngester(EnhancedFedora fedora, String dataFilePattern, String[] collections) {
        this.fedora = fedora;
        this.collections = collections;
        this.dataFilePattern = dataFilePattern;
    }

    /**
     * A factory method to return an ingester tailored to the newspaper collection in which. Data files end in ".jp2"
     * and checksums are of type ".md5".
     * @param fedora the fedora in which to ingest.
     * @return the ingester.
     */
    public static SimpleFedoraIngester getNewspaperInstance(EnhancedFedora fedora) {
        SimpleFedoraIngester ingester = new SimpleFedoraIngester(fedora, ".*\\.jp2", new String[]{"info:Batch"});
        ingester.setChecksumPostfix(".md5");
        return ingester;
    }

    @Override
    public EnhancedFedora getEnhancedFedora() {
        return fedora;
    }

    @Override
    public String getChecksumPostfix() {
        return checksumPostfix;
    }

    public void setChecksumPostfix(String checksumPostfix) {
        this.checksumPostfix = checksumPostfix;
    }

    @Override
    public String getDataFilePattern() {
        return dataFilePattern;
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
        return localname.matches(dataFilePattern);
    }



}
