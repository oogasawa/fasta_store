package jp.ac.nig.ddbj.fastastore;

import java.io.File;
import java.nio.file.Path;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;



public class FastaStoreGet {

    /** The database environment's home directory. */
    private static File envDir;

    private Environment environment;
    private EntityStore store;
    private FastaDA accessor;



    public static void main(String args[]) {
        if (args.length > 1) {
            FastaStoreGet store = new FastaStoreGet();
            store.setEnvDir(Path.of(args[0]));
            store.search(args[1]);
        }
        else {
            System.out.println("Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaStoreGet bdb_dir seqId");
        }
    }


    /** Default constructor. */
    public FastaStoreGet() {}
    

    public void search(String seqId) throws DatabaseException {

        setup();
        accessor = new FastaDA(store);

        FastaEntity result = accessor.pIdx.get(seqId);

        for (String line: result.getfastaData()) {
            System.out.println(line);
        }
        
        shutdown();
        
    }
    

    public void setEnvDir(Path dir) {
        envDir = dir.toFile();
    }

    
    public void setup() throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        // Open the environment and entity store
        environment = new Environment(envDir, envConfig);
        store = new EntityStore(environment, "FastaStore", storeConfig);

    }

    

    public void shutdown() throws DatabaseException {

        store.close();
        environment.close();
        
    }


}
