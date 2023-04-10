package jp.ac.nig.ddbj.fastastore;

import java.nio.file.Path;
import java.util.StringJoiner;


public class WebBlastFastaFiles {

    String dir;

    String filePattern;

    public String getDir() {
        return this.dir;
    }

    public String getFilePatten() {
        return this.filePattern;
    }

    public String toString() {
        StringJoiner joiner = new StringJoiner(", ");
        joiner.add("dir: " + dir);
        joiner.add("filePattern: " + filePattern);

        return joiner.toString();
    }

    
}
