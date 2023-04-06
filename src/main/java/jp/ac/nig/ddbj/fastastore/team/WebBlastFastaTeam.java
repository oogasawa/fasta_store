package jp.ac.nig.ddbj.fastastore.team;

import java.util.ArrayList;

import com.github.oogasawa.pojoactor.ActorSystem;

public class WebBlastFastaTeam {

    record FastaStore(String name, String fastaDir, String extension) {}

    ArrayList<FastaStore> stores = new ArrayList<>();
    
    int maxWorkerThreads = 5;
    
    public static void main(String[] args) {
        WebBlastFastaTeam team = new WebBlastFastaTeam();
        team.start();
    }

    public WebBlastFastaTeam() {
        stores.add(new FastaStore("DDBJ_standard", "", ""));

    }


    
    public void start() {

        ActorSystem system = new ActorSystem("webblastfasta_updator", maxWorkerThreads);

        // Initialize actors.
        system.actorOf(name, new FastaStorePut());
    }
    
}
