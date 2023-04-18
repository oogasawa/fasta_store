package jp.ac.nig.ddbj.fastastore;

import java.io.File;
import java.util.ArrayList;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class WebBlastGetEntry {

    private Environment environment;

    ArrayList<EntityStore> storeList = new ArrayList<>();


    String stores[] = {
        "DDBJ_standard",
        "DDBJ_other",
        "rRNA16S",
        "RefSeq_daily",
        "RefSeq_genomic",
        "RefSeq_RNA",
        "RefSeq_model",
        "UniProt",
        "PatentAA",
        "DAD"
    };
    
    

    /** Default constructor. */
    public WebBlastGetEntry() {}
    

    public void printFasta(FastaEntity entity) {
        for (String line : entity.getFastaData()) {
            System.out.println(line);
        }
        System.out.println("\n");
    }



    
    public void search(String[] seqIds, String envDirStr) throws DatabaseException {

        EnvironmentConfig envConfig = new EnvironmentConfig();
        environment = new Environment(new File(envDirStr), envConfig);

        ArrayList<FastaDA> accessors = setupStores();

        for (String seqId : seqIds) {

            // accessor = new FastaDA(store);
            FastaEntity result = null;
            for (FastaDA a : accessors) {
                result = a.pIdx.get(seqId);
                if (result != null) {
                    printFasta(result);
                }
            }

        }
        
        shutdownStores();

        environment.close();
    }
    

    
    public ArrayList<FastaDA> setupStores() throws DatabaseException {

        ArrayList<FastaDA> accessors = new ArrayList<>();
        
        StoreConfig storeConfig = new StoreConfig();

        // Open the environment and entity store
        EntityStore storeObj = null; 
        for (String storeName: stores) {
            storeObj = new EntityStore(environment, storeName, storeConfig);
            storeList.add( storeObj );

            accessors.add( new FastaDA(storeObj) );
        }
        
        return accessors;
    }

    

    public void shutdownStores() throws DatabaseException {

        for (EntityStore storeObj : storeList ) {
            storeObj.close();
        }
            
    }

    
}
