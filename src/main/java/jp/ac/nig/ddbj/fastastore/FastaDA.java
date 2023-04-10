package jp.ac.nig.ddbj.fastastore;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;

/** A data access class of BerkeleyDB Direct Persistent Layer (DPL).
 *
 * The main reason for the data access class to exist is to provide convenient access
 * (typically, put and get ) entries to the indexes.
 * 
 */
public class FastaDA {

    /** Primary key for the FastaEntity class. */
    PrimaryIndex<String, FastaEntity> pIdx;
    
    public FastaDA(EntityStore store) throws DatabaseException {
        
        pIdx = store.getPrimaryIndex(String.class, FastaEntity.class);
    }
    
}
