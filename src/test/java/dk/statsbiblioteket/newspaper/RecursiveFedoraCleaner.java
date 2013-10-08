package dk.statsbiblioteket.newspaper;

import dk.statsbiblioteket.doms.central.connectors.BackendInvalidCredsException;
import dk.statsbiblioteket.doms.central.connectors.BackendInvalidResourceException;
import dk.statsbiblioteket.doms.central.connectors.BackendMethodFailedException;
import dk.statsbiblioteket.doms.central.connectors.EnhancedFedora;
import dk.statsbiblioteket.doms.central.connectors.fedora.structures.FedoraRelation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: csr
 * Date: 08/10/13
 * Time: 08:25
 * To change this template use File | Settings | File Templates.
 */
public class RecursiveFedoraCleaner {

    static String hasPartRelation = "info:fedora/fedora-system:def/relations-external#hasPart";
    private static Logger log = LoggerFactory.getLogger(RecursiveFedoraCleaner.class);

    /**
     * Recursively purges fedora of every object with the given label and every object reachable
     * from them via a hasPart relation. You have been warned.
     * @param fedora the fedora instance to purge.
     * @param label the label of the root objects to be purged.
     * @param doit if false, just print information on the objects that would be deleted.
     */
    public static void cleanFedora(EnhancedFedora fedora, String label, boolean doit) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        List<String> pids = fedora.findObjectFromDCIdentifier(label);
        if (pids.isEmpty()) {
            log.info("Nothing to delete with label " + label);
            return;
        }
        log.info("About to delete " + pids.size() + " objects trees.");
        for (String pid: pids) {
            purgeObject(fedora, pid, doit);
        }
    }

    private static void purgeObject(EnhancedFedora fedora, String pid, boolean doit) throws BackendInvalidCredsException, BackendMethodFailedException, BackendInvalidResourceException {
        List<FedoraRelation> relations = fedora.getNamedRelations(pid, hasPartRelation, new Date().getTime());
        log.info("About to delete object '" + pid + "'");
        if (doit) {
            deleteSingleObject(fedora, pid);
        } else {
            log.info("Didn't actually delete object '" + pid + "'");
        }
        for (FedoraRelation relation: relations) {
            String nextPid = relation.getObject();
            purgeObject(fedora, nextPid, doit);
        }
    }

    private static void deleteSingleObject(EnhancedFedora fedora, String pid) {
        //No-op because there is no API method to do this yet
    }

}
