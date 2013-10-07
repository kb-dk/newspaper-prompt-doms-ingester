package dk.statsbiblioteket.newspaper.promptdomsingester;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.linkpatterns.LinkPattern;
import dk.statsbiblioteket.doms.central.connectors.fedora.methods.generated.Method;
import dk.statsbiblioteket.doms.central.connectors.fedora.pidGenerator.PIDGeneratorException;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.ObjectProfile;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.SearchResult;
import dk.statsbiblioteket.doms.central.connectors.fedora.templates.ObjectIsWrongTypeException;
import org.apache.commons.collections.iterators.ArrayIterator;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class TestEnhancedFedoraImpl implements EnhancedFedora {

    int objectsCreated = 0;
    int externalDatastreamsAdded = 0;
    int relationshipsAdded = 0;
    int datastreamsModified = 0;
    List<String> objectLabels = new ArrayList<String>();

    List<String> allowedDatastreams = null;

    Map<String, String> labelsAndPids = new HashMap<>();
    Map<String, List<String>> pidsAndStreams = new HashMap<>();

    public TestEnhancedFedoraImpl(List<String> allowedDatastreams) {
        this.allowedDatastreams = allowedDatastreams;
    }

    @Override
    public String cloneTemplate(String templatepid, List<String> oldIDs, String logMessage) throws BackendInvalidCredsException, BackendMethodFailedException, ObjectIsWrongTypeException, BackendInvalidResourceException, PIDGeneratorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String newEmptyObject(List<String> oldIDs, List<String> collections, String logMessage) throws BackendInvalidCredsException, BackendMethodFailedException, PIDGeneratorException {
        objectsCreated++;
        String pid = UUID.randomUUID().toString();
        if (!oldIDs.isEmpty()) {
            objectLabels.add(oldIDs.get(0));
            labelsAndPids.put(oldIDs.get(0), pid);
            pidsAndStreams.put(pid, new ArrayList<String>());
        }
        return pid;
    }

    @Override
    public ObjectProfile getObjectProfile(String pid, Long asOfTime) throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modifyObjectLabel(String pid, String name, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modifyObjectState(String pid, String stateDeleted, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void modifyDatastreamByValue(String pid, String datastream, String contents, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        checkDatastreamName(datastream);
        checkDatastreamContent(contents);
        pidsAndStreams.get(pid).add(datastream);
        datastreamsModified++;
    }

    @Override
    public void modifyDatastreamByValue(String pid, String datastream, String contents, String md5sum, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        checkDatastreamName(datastream);
        checkDatastreamContent(contents);
        pidsAndStreams.get(pid).add(datastream);
        datastreamsModified++;
    }

    @Override
    public void modifyDatastreamByValue(String pid, String datastream, String contents, String checksumType, String checksum, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        checkDatastreamName(datastream);
        checkDatastreamContent(contents);
        pidsAndStreams.get(pid).add(datastream);
        datastreamsModified++;
    }

    private void checkDatastreamName(String name) {
        if (!allowedDatastreams.contains(name)) {
            throw new RuntimeException("Unknown datastream: '" + name + "'");
        }
    }

    private void checkDatastreamContent(String content) {
        if (content.length() < 20) {
            throw new RuntimeException("Datastream is not as long as expected: '" + content + "'");
        }
    }

    @Override
    public String getXMLDatastreamContents(String pid, String datastream, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addExternalDatastream(String pid, String datastream, String filename, String permanentURL, String formatURI, String mimetype, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
       externalDatastreamsAdded++;
    }

    @Override
    public void addExternalDatastream(String pid, String datastream, String filename, String permanentURL, String formatURI, String mimetype, String checksumType, String checksum, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
       externalDatastreamsAdded++;
    }

    @Override
    public void addExternalDatastream(String pid, String datastream, String filename, String permanentURL, String formatURI, String mimetype, String md5sum, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        externalDatastreamsAdded++;
    }

    @Override
    public List<String> listObjectsWithThisLabel(String label) throws BackendInvalidCredsException, BackendMethodFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void addRelation(String pid, String subject, String predicate, String object, boolean literal, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        relationshipsAdded++;
    }

    @Override
    public List<FedoraRelation> getNamedRelations(String pid, String predicate, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<FedoraRelation> getInverseRelations(String pid, String predicate) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void deleteRelation(String pid, String subject, String predicate, String object, boolean literal, String comment) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Document createBundle(String pid, String viewAngle, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> findObjectFromDCIdentifier(String string) throws BackendInvalidCredsException, BackendMethodFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchResult> fieldsearch(String query, int offset, int pageSize) throws BackendInvalidCredsException, BackendMethodFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void flushTripples() throws BackendInvalidCredsException, BackendMethodFailedException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getObjectsInCollection(String collectionPid, String contentModelPid) throws BackendInvalidCredsException, BackendMethodFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String invokeMethod(String cmpid, String methodName, Map<String, List<String>> parameters, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Method> getStaticMethods(String cmpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<Method> getDynamicMethods(String objpid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LinkPattern> getLinks(String pid, Long asOfTime) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        String result = "";
        for (Map.Entry<String, String> entry: labelsAndPids.entrySet()) {
            List<String> datastreams = pidsAndStreams.get(entry.getValue());
            result += entry.getKey();
            result += "[";
            Iterator<String> datastreamIterator = datastreams.iterator();
            while (datastreamIterator.hasNext()) {
                result += datastreamIterator.next();
                if (datastreamIterator.hasNext()) result += ",";
            }
            result +="]\n";
        }
        return result;
    }
}
