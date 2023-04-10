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

    // /** The database environment's home directory. */
    // private static File envDir;

    private Environment environment;
    private EntityStore store;
    private FastaDA accessor;

    Pattern pSeqId = Pattern.compile("^>(\\S+?)\\|");
    Pattern pRefseqId = Pattern.compile("^>(\\S+?)\s");
    Pattern pNcbiSeqId = Pattern.compile("^>[a-z]{1,8}\\|(\\S+?)\\|");
    Pattern pDdbjSeqId = Pattern.compile(">\\S+\\|(\\S+\\.[0-9]+)\\s");
    
    private long counter = 0;
    private long start;
    
    public static void main(String[] args) {

        if (args.length > 3) {
            FastaStorePut store = new FastaStorePut();
//            store.setEnvDir(Path.of(args[0]));

            Path fastaDir = Path.of(args[1]);
            Pattern filePattern = Pattern.compile(args[2]);
            Path bdbDir = Path.of(args[0]);
            String datasetName = "FastaStore";
            store.readAll(fastaDir, filePattern, bdbDir, datasetName);
            
        } else {
            System.out.println(
                "Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaStorePut bdb_dir fasta_dir filePattern datasetName");
        }
        
    }

    
    /** Default constructor. */
    public FastaStorePut() {}

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
     * <p>There are several types of FASTA definition lines.</p>
     *
     *
     * <p>1. DDBJ style definition lines.</p>
     *
     * <pre>{@code
     * >AG122442|AG122442.1 Pan troglodytes DNA, clone: PTB-131M17.R.
     * }</pre>
     *
     * In this case, the sequnce ID is {@code AG122442.1}.
     * 
     * <p>2. NCBI style definition lines.</p>
     *
     * <pre>{@code
     * >tr|A0A6J1A7R8|A0A6J1A7R8_9ROSI RING-type E3 ubiquitin transferase OS=Herrania umbratica OX=108875 GN=LOC110415795 PE=4 SV=1
     * }</pre>
     *
     * In this case, the sequence ID is {@code A0A6J1A7R8}.
     * 
     * <p>3. RefSeq FASTA definition line.</p>
     *
     * <pre>{@code
     * >NZ_CP088252.1 Frateuria soli strain 5GH9-11 chromosome, complete genome
     * }</pre>
     *
     * This case, the sequence ID is {@code NZ_CP099252.1}.
     * 
     *
     *
     * 
     */
    public String parseSequenceId(String definitionLine) {
        String seqId = null;

        Matcher m = null;

        m = pDdbjSeqId.matcher(definitionLine);
        if (m.find()) {
            seqId = m.group(1);
            return seqId;
        }


        m = pNcbiSeqId.matcher(definitionLine);
        if (m.find()) {
            seqId = m.group(1);
            return seqId;
        }

        m = pSeqId.matcher(definitionLine);
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
     * @param fastaDir A full path to the directory where FASTA files are located. 
     * @param filePattern A Pattern object that is used for filtering relevant files in the given {@code fastaPath}.
     * @param bdbEnvDir A BerkeleyDB Environment directory.
     * @param datasetName A data set name. (e.g. DDBJ_standard, DDBJ_other, ...)
     */
    public void readAll(Path fastaDir, Pattern filePattern, Path bdbEnvDir, String datasetName) {

        logger.logp(Level.INFO, FastaStorePut.class.getName(), "readAll",
                    String.format("enter: fastaPath = %s, filePattern = %s, bdbEnvDir = %s, datasetName = %s",
                                  fastaDir.toString(), filePattern.toString(), bdbEnvDir.toString(), datasetName));

        
        setup(bdbEnvDir, datasetName);

        start = System.currentTimeMillis();

        Stream.of(fastaDir.toFile().listFiles())
            .filter(f->{
                    Matcher m = filePattern.matcher(f.getName());
                    return m.find();
                        })
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




    
    public void setup(Path bdbEnvDir, String datasetName) throws DatabaseException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        StoreConfig storeConfig = new StoreConfig();

        envConfig.setAllowCreate(true);
        storeConfig.setAllowCreate(true);

        // Open the environment and entity store
        environment = new Environment(bdbEnvDir.toFile(), envConfig);
        store = new EntityStore(environment, datasetName, storeConfig);
        
        accessor = new FastaDA(store);
    }

    

    public void shutdown() throws DatabaseException {

        store.close();
        environment.close();
        
    }



    
    
}
