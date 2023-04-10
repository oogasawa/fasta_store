package jp.ac.nig.ddbj.fastastore;

import java.util.List;
import java.util.StringJoiner;

public class WebBlastFastaDataset {

    String dataSet;

    List<WebBlastFastaFiles> fastaFiles;


    public String getDataSet() {
        return this.dataSet;
    }

    public List<WebBlastFastaFiles> getFastaFiles() {
        return this.fastaFiles;
    }


    public String toString() {
        StringJoiner joiner = new StringJoiner("\n");

        joiner.add("dataSet: " + dataSet);
        for (WebBlastFastaFiles fastaFilesInfo: fastaFiles) {
            joiner.add("fastaFiles: " + fastaFilesInfo.toString());
        }
        
        return joiner.toString();
    }
    
}
