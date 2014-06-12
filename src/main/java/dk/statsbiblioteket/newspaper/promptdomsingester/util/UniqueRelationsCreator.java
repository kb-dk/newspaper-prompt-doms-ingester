package dk.statsbiblioteket.newspaper.promptdomsingester.util;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Date;
import java.util.List;

/**
 *
 */
public class UniqueRelationsCreator {

    private EnhancedFedora fedora;
    private int maxRetries;

    /**
     *
     * @param fedora
     * @param maxRetries
     */
    public UniqueRelationsCreator(EnhancedFedora fedora, int maxRetries) {
        if (maxRetries < 0 ) {
            throw new RuntimeException("Cannot have maxRetries < 0 :"  + maxRetries);
        }
        this.fedora = fedora;
        this.maxRetries = maxRetries;
    }

    /**
     *
     * @param addRelationshipRequest
     * @return
     * @throws Exception
     */
    public int addRelationships(AddRelationshipRequest addRelationshipRequest) throws BackendMethodFailedException, BackendInvalidCredsException, BackendInvalidResourceException {
        String pid = addRelationshipRequest.getPid();
        Date lastModifiedDate = fedora.getObjectProfile(pid, null).getObjectLastModifiedDate();
        Exception lastException = null;
        for (int i=0; i <= maxRetries; i++) {
            try {
                return AddRelationshipsOneTime(addRelationshipRequest, pid, lastModifiedDate);
            } catch (Exception e) {
                //Just try again
                lastException = e;
            }
        }
        throw new BackendMethodFailedException("Failed to add relations after " + (maxRetries + 1) + "attempts.", lastException);
    }

    private int AddRelationshipsOneTime(AddRelationshipRequest addRelationshipRequest, String pid, Date lastModifiedDate) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        List<FedoraRelation> existingRelations = fedora.getNamedRelations(
                pid,
                addRelationshipRequest.getPredicate(),
                lastModifiedDate.getTime()
        );
        List<String> objects = addRelationshipRequest.getObjects();
        List<String> objectsToRemove = new ArrayList<>();
        for (FedoraRelation existingRelation: existingRelations) {
            for (String object: objects) {
                if (existingRelation.getObject().equals(object)) {
                    objectsToRemove.add(object);
                }
            }
        }
        objects.removeAll(objectsToRemove);
        RdfManipulator rdfManipulator = new RdfManipulator(fedora.getXMLDatastreamContents(pid, "RELS-EXT"));
        String predicateNS = addRelationshipRequest.getPredicate().split("#")[0] + "#";
        String predicateName = addRelationshipRequest.getPredicate().split("#")[1];
        for (String object: objects) {
             rdfManipulator.addFragmentToDescription(new RdfManipulator.Fragment(predicateNS, predicateName, object));
        }
        fedora.modifyDatastreamByValue(pid, "RELS-EXT",
                null,
                null,
                rdfManipulator.toString().getBytes(),
                new ArrayList<String>(), "application/rdf+xml",
                addRelationshipRequest.getComment(),
                lastModifiedDate.getTime());
        return objects.size();
    }
}
