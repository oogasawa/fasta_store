package jp.ac.nig.ddbj.fastastore.team;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.github.oogasawa.pojoactor.ActorRef;
import com.github.oogasawa.pojoactor.ActorSystem;

import org.yaml.snakeyaml.Yaml;

import jp.ac.nig.ddbj.fastastore.FastaStorePut;


public class WebBlastFastaTeam {

    private static final Logger logger = Logger.getLogger("jp.ac.nig.ddbj.fastastore.team.WebBlastFastaTeam");
    
    /** A BerkeleyDB Environment.
     *
     * It is assumed that all actors use the same BDB Environment.
     */
    String bdbEnvDir = null;
    
    int maxWorkerThreads = 2;

    Map<String, Object> naDatasetInfo = null;

    Map<String, Object> aaDatasetInfo = null;
    
    
    public static void main(String[] args) {

        if (args.length > 0) {

            WebBlastFastaTeam team = new WebBlastFastaTeam();
            team.setBdbEnvDir(args[0]);
            team.readFastaInfo();
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

        Yaml yaml = new Yaml();

        // Read fasta_na.yaml (nucleic acid sequence info)
        URL url = this.getClass().getClassLoader().getResource("fasta_na.yaml");
        try (InputStream input = url.openStream()) {
            naDatasetInfo =  yaml.load(input);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can not open 'src/resources/fasta_na.yaml' file", e);
        }

        // Read fasta_aa.yaml (amino acid sequence info)
        url = this.getClass().getClassLoader().getResource("fasta_aa.yaml");
        try (InputStream input = url.openStream()) {
            aaDatasetInfo =  yaml.load(input);

            // This code is for a testing purpose.
            // 
            // @SuppressWarnings("unchecked")
            // List<Object> dsList = (List<Object>)aaDatasetInfo.get("datasets");
            // for (Object elem: dsList) {

            //     @SuppressWarnings("unchecked")
            //     Map<String, Object> ds = (Map<String, Object>)elem;
            //     System.out.println((String)ds.get("dataSet"));
            // }
            
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Can not open 'src/resources/fasta_aa.yaml' file", e);
        }
        
    }



    // /** Returns a list of dataset names defined in the YAML file.
    //  *
    //  * YAML files are assumed to be placed in the {@code src/main/resources} directory.
    //  *
    //  * @param yamlFilename File name of the YAML file.
    //  */
    // public ArrayList<String> datasetNames(String yamlFilename) {
        
    //     @SuppressWarnings("unchecked")
    //     List<Object> dsList = (List<Object>) aaDatasetInfo.get("datasets");
    //     for (Object elem : dsList) {

    //         @SuppressWarnings("unchecked")
    //         Map<String, Object> ds = (Map<String, Object>) elem;

    //         String datasetName = (String) ds.get("dataSet");
        
    // }


    

    public void initActors(ActorSystem system) {

        initActors(system, this.naDatasetInfo);
        initActors(system, this.aaDatasetInfo);
    }

    
    public void initActors(ActorSystem system, Map<String, Object> datasetInfo) {
        
        @SuppressWarnings("unchecked")
        List<Object> dsList = (List<Object>) datasetInfo.get("datasets");
        for (Object elem : dsList) {

            @SuppressWarnings("unchecked")
            Map<String, Object> ds = (Map<String, Object>) elem;

            String datasetName = (String) ds.get("dataSet");

            ActorRef<FastaStorePut> actor = system.actorOf(datasetName, new FastaStorePut());

        }

    }


    
    public void setBdbEnvDir(String dir) {
        this.bdbEnvDir = dir;
    }

    
    @SuppressWarnings("unchecked")
    public void start() {

        ActorSystem system = new ActorSystem("webblastfasta_updator", maxWorkerThreads);

        // Initialize actors. (An actor correspond to a data set.)
        this.initActors(system);

        
        // Assign FASTA files infomation to be processed to each actor.
        String fastaBaseDir = (String)naDatasetInfo.get("fastaBaseDir");
        List<Object> dsList = (List<Object>)naDatasetInfo.get("datasets");
        for (Object elem: dsList) {

            Map<String, Object> ds = (Map<String, Object>) elem;
            String datasetName =(String) ds.get("dataSet");
            List<Object> fastaFiles = (List<Object>)ds.get("fastaFiles");
            for (Object fastaInfo: fastaFiles) {
                Map<String, String> info = (Map<String, String>)fastaInfo;

                Path fastaDir = Path.of(fastaBaseDir).resolve(Path.of(info.get("dir")));
                Pattern filePattern = Pattern.compile(info.get("filePattern"));

                ActorRef<FastaStorePut> actor = system.getActor(datasetName);
                actor.tell(a->a.readAll(fastaDir, filePattern, this.bdbEnvDir, datasetName));

            }
        }


        
        
        // try {

        //     ArrayList<CompletableFuture<Void>> jobList = new ArrayList<>();
        //     WebBlastFastaInfo info = new WebBlastFastaInfo();
        //     info.setFastaBaseDir(Path.of(System.getenv("HOME")).resolve("blast_test_dataset"));
        //     for (WebBlastFastaInfo.FastaInfo r : info.getFastaDirs()) {

        //         // Initialize actors.
        //         ActorRef<FastaStorePut> actor = system.actorOf(r.dsName(), new FastaStorePut());
        //         actor.tell(a -> a.setEnvDir(Path.of(bdbDir).resolve(r.dsName()))).get();

        //         // Execute actor's jobs.
        //         Path fastaDir = info.getFastaBaseDir().resolve(r.fastaDir());
        //         logger.info("FASTA directory (resolved): " + fastaDir.toString());
        //         jobList.add(actor.tell(a -> a.readAll(fastaDir, r.extension()), system.getWorkStealingPool()));
        //     }

        //     // wait for all the jobs are completed.
        //     for (CompletableFuture<Void> job : jobList) {
        //         job.get();
        //     }

        // } catch (InterruptedException e) {
        //     logger.log(Level.SEVERE, "interrupted." , e);
        // } catch (ExecutionException e) {
        //     logger.log(Level.SEVERE, "Unexpected Error." , e);
        // }

        system.terminate();
    }




    
}
