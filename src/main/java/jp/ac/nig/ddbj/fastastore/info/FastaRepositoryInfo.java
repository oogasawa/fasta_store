package jp.ac.nig.ddbj.fastastore.info;

import java.nio.file.Path;
import java.util.List;

public class FastaRepositoryInfo {

    String fastaBaseDir;

    List<DatasetInfo> datasets;

    
    public String getFastaBaseDir() {

        if (fastaBaseDir.startsWith("/")) { // full path is given.
            return fastaBaseDir;            
        }
        else { // it is interpreted as a path relative to the home.
            return Path.of(System.getenv("HOME")).resolve(fastaBaseDir).toString();
        }

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
