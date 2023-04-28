package jp.ac.nig.ddbj.fastastore.team;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.github.oogasawa.pojoactor.ActorRef;
import com.github.oogasawa.pojoactor.ActorSystem;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import jp.ac.nig.ddbj.fastastore.FastaStorePut;
import jp.ac.nig.ddbj.fastastore.info.DatasetInfo;
import jp.ac.nig.ddbj.fastastore.info.FastaFilesInfo;
import jp.ac.nig.ddbj.fastastore.info.FastaRepositoryInfo;


public class WebBlastFastaTeam {

    Logger logger = null;
    //private static final Logger logger = Logger.getLogger("jp.ac.nig.ddbj.fastastore.team.WebBlastFastaTeam");
    
    /** A BerkeleyDB Environment.
     *
     * It is assumed that all actors use the same BDB Environment.
     */
    Path bdbEnvDir = null;
    private Environment environment;
    
    int maxWorkerThreads = 1;

    FastaRepositoryInfo naRepositoryInfo = null;
    FastaRepositoryInfo aaRepositryInfo = null;
    

    public static class Builder {
        Path bdbEnvDir = null;
        int maxWorkerThread = 10;
        
        public Builder(Path bdbEnvDir) {
            this.bdbEnvDir = bdbEnvDir;
        }

        public Builder workerThreads(int num) {
            this.maxWorkerThread = num;
            return this;
        }
        
        public WebBlastFastaTeam build() {
            WebBlastFastaTeam team = new WebBlastFastaTeam();
            team.logger = Logger.getLogger("WebBlastFastaTeam");
            team.setBdbEnvDir(bdbEnvDir);
            team.setMaxWorkerThreads(maxWorkerThread);
            team.readFastaInfo();

            return team;
        }
    }


    
    public static void main(String[] args) {

        if (args.length > 0) {

            WebBlastFastaTeam team
                = new WebBlastFastaTeam.Builder(Path.of(args[0]))
                .build();

            
            // start the calculation.
            team.start();
        }
        else {
            System.err.println("Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.team.WebBlastFastaTeam  bdbDir");
        }
    }


    /** Default constructor. */
    public WebBlastFastaTeam() {  }

    
    
    /** Reads data set information from the YAML files located in {@code src/main/resources} in the jar file.
     *
     * The data set information is stored in {@link naDatasetInfo} and {@link aaDatasetInfo} field in this class.
     */
    public void readFastaInfo() {

        Yaml yaml = new Yaml(new Constructor(FastaRepositoryInfo.class));

        // Read fasta_na.yaml (nucleic acid sequence info)
        URL url = this.getClass().getClassLoader().getResource("fasta_na.yaml");
        logger.info("Reading fasta_na.yaml");
        try (InputStream input = url.openStream()) {
            naRepositoryInfo =  yaml.load(input);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can not open 'src/resources/fasta_na.yaml' file", e);
        }

        // Read fasta_aa.yaml (amino acid sequence info)
        url = this.getClass().getClassLoader().getResource("fasta_aa.yaml");
        logger.info("Reading fasta_aa.yaml");
        try (InputStream input = url.openStream()) {
            aaRepositryInfo =  yaml.load(input);            
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can not open 'src/resources/fasta_aa.yaml' file", e);
        }
        
    }


    /** Generate the actors for the all dataset specified in {@code naDataSetInfo} and {@code aaDataSetInfo} fields.
     *  
     * It is assumed that this method will be called after the {@code readFastaInfo} method has been called
     * and the {@code naDataSetInfo} and {@code aaDataSetInfo} fields have been set.
     * 
     * @param system The ActorSystem to which all generated actors belong.
     */
    public void initActors(ActorSystem system) {

        initActors(system, this.naRepositoryInfo);
        initActors(system, this.aaRepositryInfo);
    }



    private void initActors(ActorSystem system, FastaRepositoryInfo repInfo) {
        
        for (DatasetInfo ds : repInfo.getDatasets()) {
            String datasetName = ds.getDataSet();
            ActorRef<FastaStorePut> actor
                = system.actorOf(datasetName,
                                 new FastaStorePut.Builder()
                                 .loggerName(datasetName)
                                 .build());
        }
    }


    
    public void setBdbEnvDir(Path dir) {
        this.bdbEnvDir = dir;
    }


    public void setMaxWorkerThreads(int num) {
        this.maxWorkerThreads = num;
    }
    
    
    public void start() {

        setupEnvironment(bdbEnvDir);
        
        ActorSystem system = new ActorSystem("webblastfasta_updator", maxWorkerThreads);

        // Initialize actors. (An actor correspond to a data set.)
        this.initActors(system);

        // Start job execution.
        ArrayList<CompletableFuture<Void>> states = new ArrayList<>();
        states.addAll(invoke2(this.naRepositoryInfo, system));
        states.addAll(invoke2(this.aaRepositryInfo, system));

        // Wait for all the jobs to finish.
        try {
            CompletableFuture.allOf(states.toArray(new CompletableFuture[states.size()])).get();
            
        }
        catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Interruption.", e);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected Error.", e);
        }

        shutdownEnvironment();
        
        system.terminate();
    }


    public ArrayList<CompletableFuture<Void>> invoke(FastaRepositoryInfo repInfo, ActorSystem system) {

        ArrayList<CompletableFuture<Void>> result = new ArrayList<>();
        
        // Assign FASTA files infomation to be processed to each actor.
        String fastaBaseDir = repInfo.getFastaBaseDir();
        List<DatasetInfo> dsList = repInfo.getDatasets();
        for (DatasetInfo ds: dsList) {
            
            String datasetName = ds.getDataSet();
            List<FastaFilesInfo> fastaFiles = ds.getFastaFiles();
            int counter = 0;
            for (FastaFilesInfo fastaInfo: fastaFiles) {

                // if (counter++ > 2) {
                //     break;
                // }
                
                Path fastaDir = Path.of(fastaBaseDir).resolve(Path.of(fastaInfo.getDir()));
                Pattern filePattern = Pattern.compile(fastaInfo.getFilePattern());

                
                ActorRef<FastaStorePut> actor = system.getActor(datasetName);
                // logger.logp(Level.INFO, WebBlastFastaTeam.class.getName(), "invoke",
                //             String.format("datasetName = %s, fastaDir = %s, filePattern = %s", datasetName, fastaDir.toString(), filePattern.toString()));
                result.add(actor.tell(a->a.readAll(fastaDir, filePattern, this.bdbEnvDir, datasetName), system.getWorkStealingPool()));

            }
        }

        return result;

    }



    public ArrayList<CompletableFuture<Void>> invoke2(FastaRepositoryInfo repInfo, ActorSystem system) {

        ArrayList<CompletableFuture<Void>> result = new ArrayList<>();
        
        // Assign FASTA files infomation to be processed to each actor.
        String fastaBaseDir = repInfo.getFastaBaseDir();
        List<DatasetInfo> dsList = repInfo.getDatasets();
        for (DatasetInfo ds: dsList) {
            String datasetName = ds.getDataSet();
            ActorRef<FastaStorePut> actor = system.getActor(datasetName);
            result.add(actor.tell(a->a.readAll(Path.of(fastaBaseDir), ds, this.environment), system.getWorkStealingPool()));   
        }
        return result;

    }


    
        
    public void setupEnvironment(Path bdbEnvDir)  {

        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();

            envConfig.setAllowCreate(true);

            // Open the environment and entity store
            //Path envDir = bdbEnvDir.resolve(Path.of(datasetName));
            if (!bdbEnvDir.toFile().exists()) {
                bdbEnvDir.toFile().mkdirs();
            }
            environment = new Environment(bdbEnvDir.toFile(), envConfig);

        } catch (DatabaseException e) {
            logger.log(Level.SEVERE, "Unexpected DatabaseException", e);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected Exception", e);
        }    
    }



    public void shutdownEnvironment()  {

        String envStr = environment.toString();
        logger.log(Level.INFO, "this.environment.toString() = " + envStr);
        try {

            if (environment != null) {
                environment.cleanLog();
                environment.close();
            }
        }
        catch (DatabaseException e) {
            logger.log(Level.SEVERE, "Unexpected DatabaseException", e);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected Exception", e);
        }
    }

    
}
