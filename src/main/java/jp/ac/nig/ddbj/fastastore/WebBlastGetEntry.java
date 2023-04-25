package jp.ac.nig.ddbj.fastastore;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;

public class WebBlastGetEntry {

    private static final Logger logger = Logger.getLogger(WebBlastGetEntry.class.getName());
    
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



    
    public void search(List<String> seqIds, String envDirStr) throws DatabaseException {

        logger.info(String.format("envDirStr = %s", envDirStr));
        
        EnvironmentConfig envConfig = new EnvironmentConfig();
        environment = new Environment(new File(envDirStr), envConfig);

        logger.info(String.format("Finish initializing envDirStr = %s", envDirStr));
        
        ArrayList<FastaDA> accessors = setupStores();
        
        for (String seqId : seqIds) {

            // accessor = new FastaDA(store);
            FastaEntity result = null;
            int counter = 0;
            for (FastaDA a : accessors) {
                logger.info(String.format("Searching %d-th store", ++counter));
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

            logger.info("Setting up " + storeName);
            storeObj = new EntityStore(environment, storeName, storeConfig);
            storeList.add( storeObj );

            accessors.add( new FastaDA(storeObj) );
        }
        logger.info("finishing setupStores()");
        return accessors;
    }

    

    public void shutdownStores() throws DatabaseException {

        for (EntityStore storeObj : storeList ) {
            storeObj.close();
        }
            
    }

    
}
