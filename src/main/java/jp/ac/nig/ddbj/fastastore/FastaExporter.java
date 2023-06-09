package jp.ac.nig.ddbj.fastastore;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityCursor;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;


/** Exports all FASTA entries in the BerkeleyDB.
 */
public class FastaExporter {

    private static final Logger logger = Logger.getLogger("ddbj.FastaExporter");
    
    /** The database environment's home directory. */
    private static File envDir;

    private Environment environment;
    private EntityStore store;
    //private FastaDA accessor;
    

    // public static void main(String args[]) {
    //     if (args.length > 1) {
    //         FastaExporter store = new FastaExporter();
    //         store.setEnvDir(Path.of(args[0]));
    //         try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(args[11])))) {
    //             store.export(pw);
    //         }
    //         catch (IOException e) {
    //             logger.log(Level.SEVERE, "Error on output file: " + args[1], e);
    //         }
    //     }
    //     else {
    //         System.out.println("Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaExporter bdb_dir output_fasta");
    //     }
    // }


    /** A constructor.
     *
     * @param dir A base directory of a BerkeleyDB environment.
      */
    public FastaExporter() {}


    public void setEnvDir(Path dir) {
        envDir = dir.toFile();
    }

    
    public void export(String fileName, String dataset)  {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(fileName)))) {
            this.export(pw, dataset);
        }
        catch (IOException e) {
            logger.log(Level.SEVERE, "Error on output file: " + fileName, e);
        }

    }


    public void export(PrintWriter writer, String dataset)  {

        setup(dataset);
        //accessor = new FastaDA(store);
        
        PrimaryIndex<String, FastaEntity> pIndex = store.getPrimaryIndex(String.class, FastaEntity.class);
        EntityCursor<FastaEntity> pi_cursor = pIndex.entities();

        try {
            int count = 0;
            for (FastaEntity entity: pi_cursor) {
                int state = 0;
                if (++count % 10000 == 0) {
                    state = 1;
                    logger.info(String.format("count : %d", count));
                }
                for (String line: entity.getFastaData()) {
                    if (state > 0) {
                        logger.info(String.format("line : %d, %s", count, line));
                        state = 0;
                    }
                     writer.println(line);
                }
                writer.println("");
            }
        }
        catch (DatabaseException e) {
            logger.log(Level.SEVERE, "Unexpected error", e);
        }
        finally {
            pi_cursor.close();
        }
        
        shutdown();
        
    }
    
    
    public void setup(String dataset) throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        // envConfig.setAllowCreate(true);
        // storeConfig.setAllowCreate(true);

        // Open the environment and entity store
        environment = new Environment(envDir, envConfig);
        store = new EntityStore(environment, dataset, storeConfig);

    }


    public void shutdown() throws DatabaseException {
        store.close();
        environment.close();  
    }


}
