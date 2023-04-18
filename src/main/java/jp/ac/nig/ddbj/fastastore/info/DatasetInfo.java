package jp.ac.nig.ddbj.fastastore.info;

import java.util.List;

public class DatasetInfo {

    String dataSet;

    List<FastaFilesInfo> fastaFiles;


    
    public String getDataSet() {
        return dataSet;
    }

    public void setDataSet(String dataSet) {
        this.dataSet = dataSet;
    }

    public List<FastaFilesInfo> getFastaFiles() {
        return fastaFiles;
    }

    public void setFastaFiles(List<FastaFilesInfo> fastaFiles) {
        this.fastaFiles = fastaFiles;
    }



    
}
