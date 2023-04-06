package jp.ac.nig.ddbj.fastastore;

import java.nio.file.Path;
import java.util.ArrayList;

public class WebBlastFastaDirs {

    /** Base Directory of FASTA data.
     *
     * Example:
     * <ul>
     * <li>{@code /home/w3wabi/BLAST}</li>
     * <li>{@code /home/you/blast_test_data}</li>
     * </ul>
     */ 
    Path baseDir;

    /** 
     */
    record FastaDir(String dsName, Path fastaDir, String extension) {}

    ArrayList<FastaDir> fastaDirs = new ArrayList<>();
    
    public WebBlastFastaDirs() {
        fastaDirs.add(new FastaDir("DDBJ_standard", Path.of(), "seq.gz")); 
    }

    public void setBaseDir(Path dir) {
        this.baseDir = dir;
    }
    
}
