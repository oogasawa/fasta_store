package jp.ac.nig.ddbj.fastastore.team;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.oogasawa.pojoactor.ActorRef;
import com.github.oogasawa.pojoactor.ActorSystem;

import jp.ac.nig.ddbj.fastastore.FastaStorePut;
import jp.ac.nig.ddbj.fastastore.WebBlastFastaDirs;

public class WebBlastFastaTeam {

    private static final Logger logger = Logger.getLogger("jp.ac.nig.ddbj.fastastore.team.WebBlastFastaTeam");
    
    /** A BerkeleyDB Environment.
     *
     * It is assumed that all actors use the same BDB Environment.
     */
    String bdbDir = null;
    
    int maxWorkerThreads = 5;
    
    public static void main(String[] args) {

        if (args.length > 0) {
        
        WebBlastFastaTeam team = new WebBlastFastaTeam();
        team.setBdbDir(args[0]);
        team.start();
        }
        else {
            System.err.println("Usage: java -cp fastastore-fat.jar jp.ac.nig.ddbj.fastastore.team.WebBlastFastaTeam  bdbDir");
        }
    }


    public void setBdbDir(String dir) {
        this.bdbDir = dir;
    }
    
    
    public void start() {

        ActorSystem system = new ActorSystem("webblastfasta_updator", maxWorkerThreads);

        try {

            ArrayList<CompletableFuture<Void>> jobList = new ArrayList<>();
            WebBlastFastaDirs info = new WebBlastFastaDirs();
            info.setFastaBaseDir(Path.of(System.getenv("HOME")).resolve("blast_test_dataset"));
            for (WebBlastFastaDirs.FastaInfo r : info.getFastaDirs()) {

                // Initialize actors.
                ActorRef<FastaStorePut> actor = system.actorOf(r.dsName(), new FastaStorePut());
                actor.tell(a -> a.setEnvHome(Path.of(bdbDir).resolve("r.dsName()"))).get();

                // Execute actor's jobs.
                Path fastaDir = info.getFastaBaseDir().resolve(r.fastaDir());
                logger.info("FASTA directory (resolved): " + fastaDir.toString());
                jobList.add(actor.tell(a -> a.readAll(fastaDir, r.extension()), system.getWorkStealingPool()));
            }

            // wait for all the jobs are completed.
            for (CompletableFuture<Void> job : jobList) {
                job.get();
            }

        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "interrupted." , e);
        } catch (ExecutionException e) {
            logger.log(Level.SEVERE, "Unexpected Error." , e);
        }

        system.terminate();
    }
    
}
