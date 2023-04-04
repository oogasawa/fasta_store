package jp.ac.nig.ddbj.fastastore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.StoreConfig;



public class FastaStoreGet {

    /** The database environment's home directory. */
    private static File envHome;

    private Environment environment;
    private EntityStore store;
    private FastaDA accessor;



    public static void main(String args[]) {
        if (args.length > 1) {
            FastaStoreGet store = new FastaStoreGet(Path.of(args[0]));
            store.search(args[1]);
        }
        else {
            System.out.println("Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaStoreGet bdb_dir seqId");
        }
    }


    public FastaStoreGet(Path dir) {
        envHome = dir.toFile();
    }


    public void search(String seqId) throws DatabaseException {

        setup();
        accessor = new FastaDA(store);

        FastaEntity result = accessor.pIdx.get(seqId);

        for (String line: result.getfastaData()) {
            System.out.println(line);
        }
        
        shutdown();
        
    }
    
    
    public void setup() throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        // Open the environment and entity store
        environment = new Environment(envHome, envConfig);
        store = new EntityStore(environment, "FastaStore", storeConfig);

    }

    

    public void shutdown() throws DatabaseException {

        store.close();
        environment.close();
        
    }


}
