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


/** Placing objects in an entity store.
 *
 */
public class FastaStorePut {

    private static final Logger logger = Logger.getLogger("jp.ac.nig.ddbj.fastastore.FastaStorePut");

    /** The database environment's home directory. */
    private static File envHome;

    private Environment environment;
    private EntityStore store;
    private FastaDA accessor;


    public static void main(String[] args) {
        if (args.length > 0) {
            FastaStorePut store = new FastaStorePut(Path.of(args[0]));
        }
        else {
            System.out.println("Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaStorePut bdb_dir");
        }
    }

    
    public FastaStorePut(Path envPath) {
        envHome = envPath.toFile();
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



    public void readFasta(BufferedReader fastaReader) {

        logger.logp(Level.INFO, FastaStorePut.class.getName(), "readFasta", "enter");
        
        Pattern pDescLine = Pattern.compile("^>(\\S+)\s+(.+)$");
        Pattern pSeqLine = Pattern.compile("^([ a-zA-Z]+)$");
        Pattern pNullLine = Pattern.compile("^\s*$");

        FastaEntity entity = null;
        
        String key = null;        
        String line = null;

        try {
            while ((line = fastaReader.readLine()) != null) {

                // description line.
                Matcher m = pDescLine.matcher(line);
                if (m.find()) {

                    if (key != null) {
                        //if( accessor.pIdx.get(key) != null) {
                        //    accessor.pIdx.delete(key);
                        //}
                        accessor.pIdx.put(entity);
                    }

                    entity = new FastaEntity();
                    String seqId = entity.parseSequenceId(line);
                    key = seqId;

                    entity.setSequenceId(seqId);
                    entity.addLine(line);

                    continue;
                }

                // empty (null) line
                m = pNullLine.matcher(line);
                if (m.matches()) {
                    continue;
                }

                // sequence line
                m = pSeqLine.matcher(line);
                if (m.matches()) {
                    entity.addLine(line);
                    continue;
                }

                logger.log(Level.WARNING, "Unexpected line in the FASTA: " + line);
            }

            accessor.pIdx.put(entity);
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Exception occurred while reading line: " + line, e);
        }
        
    }
    


    /** Reads all FASTA files in a given directory to a BDB database in date order.
     *
     * If there are duplicate sequence IDs, the older data will be overwritten by the later loaded one
     * (i.e., the one with the newer file update date/time).
     *
     * Therefore, sequence IDs will not overlap on the resulting database.
     * 
     * @param fastaBasePath A directory where FASTA files are located.
     * @param ext An extension. (e.g. ".fna.gz")
     */
    public void readAll(Path fastaBasePath, String ext) {

        logger.logp(Level.INFO, FastaStorePut.class.getName(), "readAll", "enter");
        logger.log(Level.INFO, "fastaBasePath: " + fastaBasePath.toString());

        setup();
        
        Stream.of(fastaBasePath.toFile().listFiles())
            .filter(f->f.getName().endsWith(ext))
            .sorted((f1, f2)->{
                    if ( f1.lastModified() > f2.lastModified() )
                        return 1;
                    else if (f1.lastModified() == f2.lastModified())
                        return 0;
                    else
                        return -1;
                })
            .forEach((f)->{
                    BufferedReader br;
                    try {
                        if (isGz(f)) {
                            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(f))));
                        }
                        else {
                            br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
                        }
                        this.readFasta(br);
                    }
                    catch (IOException e) {
                        logger.log(Level.SEVERE, "Can not read: " + f.toString(), e);
                    }
                });

        shutdown();
        
        logger.logp(Level.INFO, FastaStorePut.class.getName(), "readAll", "exit");
    }


    /** Checks if a given file is a gzipped file.
     *
     * It is judged by whether the extension is "gz" or not.
     * 
     * @param file A file to be checked.
     * @return true if the given file is a gipped file. Otherwise false.
      */
    public boolean isGz(File file) {
        if (file.toString().endsWith(".gz")) {
            return true;
        }
        else {
            return false;
        }
    }

    
    
}
