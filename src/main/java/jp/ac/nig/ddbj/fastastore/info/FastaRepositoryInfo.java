package jp.ac.nig.ddbj.fastastore.info;

import java.util.List;

public class FastaRepositoryInfo {

    String fastaBaseDir;

    List<DatasetInfo> datasets;

    
    public String getFastaBaseDir() {
        return fastaBaseDir;
    }

    public void setFastaBaseDir(String fastaBaseDir) {
        this.fastaBaseDir = fastaBaseDir;
    }

    public List<DatasetInfo> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetInfo> datasets) {
        this.datasets = datasets;
    }

    
}
