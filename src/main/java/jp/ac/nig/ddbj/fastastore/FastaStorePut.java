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

    Pattern pSeqId = Pattern.compile("^>(\\S+?)\\|");
    Pattern pRefseqId = Pattern.compile("^>(\\S+?)\s");
    
    private long counter = 0;
    private long start;
    
    public static void main(String[] args) {

        if (args.length > 2) {
            FastaStorePut store = new FastaStorePut();
            store.setEnvHome(Path.of(args[0]));
            store.setup();
            store.readAll(Path.of(args[1]), args[2]);
            store.shutdown();
        } else {
            System.out.println(
                    "Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaStorePut bdb_dir fasta_dir fasta_ext");
        }
        
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


    

    /** Parses a definition line and returns a sequenceId.
     *
     * There are several types of FASTA definition lines.
     *
     * 1. RefSeq FASTA definition line.
     *
     * <pre>{@code
     * >NZ_CP088252.1 Frateuria soli strain 5GH9-11 chromosome, complete genome
     * }</pre>
     *
     * This case, the sequence ID is {@code NZ_CP099252.1}.
     * 
     * 2. DDBJ nucleic acids definition line.
     * <pre>{@code
     * >CP100557|CP100557.1 Gallus gallus breed Huxu chromosome 3
     * }</pre>
     *
     * In this example, the sequence ID is {@code CP100557}.
     */
    public String parseSequenceId(String definitionLine) {
        String seqId = null;
        
        Matcher m = pSeqId.matcher(definitionLine);
        if (m.find()) {
            seqId = m.group(1);
            return seqId;
        }

        m = pRefseqId.matcher(definitionLine);
        if (m.find()) {
            seqId = m.group(1);
            return seqId;
        }
        
        
        return seqId;
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

        start = System.currentTimeMillis();
        
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
                        logger.log(Level.INFO, "filename: " + f.toString());
                        this.readFasta(br);
                    }
                    catch (IOException e) {
                        logger.log(Level.SEVERE, "Can not read: " + f.toString(), e);
                    }
                });

        shutdown();
        
        logger.logp(Level.INFO, FastaStorePut.class.getName(), "readAll", "exit");
    }




    
    public void readFasta(BufferedReader fastaReader) {

        logger.logp(Level.INFO, FastaStorePut.class.getName(), "readFasta", "enter");

        Pattern pDescLine = Pattern.compile("^>(\\S+)\s+(.+)$");
        Pattern pSeqLine = Pattern.compile("^([ a-zA-Z]+)$");
        Pattern pNullLine = Pattern.compile("^\s*$");


        long sequenceLineNumber = 0;
        
        FastaEntity entity = null;

        String key = null;
        String line = null;

        try {
            while ((line = fastaReader.readLine()) != null) {

                // description line.
                Matcher m = pDescLine.matcher(line);
                if (m.find()) {

                    sequenceLineNumber = 0;
                    if (counter++ % 10000 == 0) {
                        long end = System.currentTimeMillis();
                        logger.log(Level.INFO, 
                                String.format("entry count, elapsed time = %d\t%d", counter, (end - start) / 1000L));
                        logger.log(Level.INFO, line);
                        String seqId = parseSequenceId(line);
                        logger.log(Level.INFO, seqId);
                    }

                    
                    if (key != null) {
                        // if( accessor.pIdx.get(key) != null) {
                        // accessor.pIdx.delete(key);
                        // }
                        accessor.pIdx.put(entity);

                    }

                    entity = new FastaEntity();
                    String seqId = parseSequenceId(line);
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

                    if (++sequenceLineNumber % 100000 == 0) {
                        logger.log(Level.INFO, String.format("sequenceID, sequenceLineNumber = %s\t%d", key, sequenceLineNumber));
                    }
                    
                    continue;
                }

                logger.log(Level.WARNING, "Unexpected line in the FASTA: " + line);
            }

            
            accessor.pIdx.put(entity);
            
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception occurred while reading line: " + line, e);
        } 
        
    }
    

    /** Sets the base directory of BerkeleyDB Environment.
     *
     * @param envPath A Path object that represents the environment base directory.
     */
    public void setEnvHome(Path envPath) {
        envHome = envPath.toFile();
        if (!envHome.exists()) {
            envHome.mkdirs();
        }        
    }


    
    public void setup() throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        // Open the environment and entity store
        environment = new Environment(envHome, envConfig);
        store = new EntityStore(environment, "FastaStore", storeConfig);

        accessor = new FastaDA(store);
    }

    

    public void shutdown() throws DatabaseException {

        store.close();
        environment.close();
        
    }



    
    
}
