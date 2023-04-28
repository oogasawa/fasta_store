package jp.ac.nig.ddbj.fastastore.info;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.github.oogasawa.pojobdd.BddUtil;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import jp.ac.nig.ddbj.fastastore.info.FastaRepositoryInfo;

public class FastaRepositoryInfoSpec {


    public static boolean exec() {

        String docId = BddUtil.documentId("FastaRepositoryInfoSpec");
        Path mdPath = Path.of(docId, docId + ".md");
        
        try (PrintStream out = BddUtil.newPrintStream(mdPath)) {
            // Checks if all the tests are succeeded.
            List<Boolean> results = new ArrayList<Boolean>();

            out.println(BddUtil.yamlHeader(docId, "AllTrue"));
            results.add(readFastaInfoSpec01(out));

            out.flush();
            return BddUtil.allTrue(results);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    
    public static boolean readFastaInfoSpec01(PrintStream out) {

        // Description
        String description = """
            
            ## Reading FASTA Repository Info from YAML files.

            ### Scenario 01: Reading the information from resources directory.
            
            - Given a FASTA Repository information YAML file in the resoureces directory
            - When the YAML file is read by SneakYAML constructor
            - Then returns true if all the elements of given list are true, otherwise false.

            Code:

            ```
            {{snippet}}
            ```

            Result:
                                           
            """;

        // Reality
        // %begin snippet : readFastaInfoSpec01

        // 1. Load data from a YAML file.
        Yaml yaml = new Yaml(new Constructor(FastaRepositoryInfo.class));

        FastaRepositoryInfo repInfo = null;
        URL url = FastaRepositoryInfoSpec.class.getClassLoader().getResource("fasta_na.yaml");
        try (InputStream input = url.openStream()) {
            repInfo =  yaml.load(input);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        // 2. Get fields from the repInfo object.
        StringJoiner answers = new StringJoiner("\n");
        answers.add(String.format("fastaBaseDir = %s", repInfo.getFastaBaseDir()));

        for (DatasetInfo dsInfo: repInfo.getDatasets()) {
            answers.add(String.format("dataSet = %s", dsInfo.getDataSet()));
        }

        DatasetInfo dsInfo = repInfo.getDatasets().get(0);
        answers.add(String.format("dataSet (an example) = %s", dsInfo.getDataSet()));
        for (FastaFilesInfo fastaInfo: dsInfo.getFastaFiles()) {
            answers.add(String.format("dir = %s, filePattern = %s", fastaInfo.getDir(), fastaInfo.getFilePattern()));
        }
        
        String result = answers.toString();
        // %end snippet : readFastaInfoSpec01

        String snippet = BddUtil.readSnippet(
                            Path.of("src/test/java/jp/ac/nig/ddbj/fastastor/info/FastaRepositoryInfoSpec.java"),
                            "readFastaInfoSpec01");
        description = description.replace("{{snippet}}", snippet);
        out.println(description);
        

        // Expectations
        String[] expectations = {
                "fastaBaseDir = /home/oogasawa/blast_test_dataset", "dataSet = DDBJ_standard", "dataSet = DDBJ_other",
                "dataSet = rRNA16S", "dataSet = RefSeq_daily", "dataSet = RefSeq_genomic", "dataSet = RefSeq_RNA",
                "dataSet = RefSeq_model", "dataSet (an example) = DDBJ_standard",
                "dir = na/ddbj/fasta, filePattern = .*ddbjhum.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjpri.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjrod.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjmam.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjvrt.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjinv.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjpln.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjbct.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjvrl.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjphg.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjenv.+\\.seq\\.gz",
                "dir = na/ddbj/fasta, filePattern = .*ddbjsyn.+\\.seq\\.gz", };

        expectations[0] = replaceFastaBaseDir(expectations[0]);
        String expectation = String.join("\n", expectations);

        // Check the answer.
        boolean passed = BddUtil.assertTrue(out, expectation, result);
        assert passed;
        return passed;


        //return true;
    }


    private static String replaceFastaBaseDir(String line) {
        Pattern pDir = Pattern.compile("\\/home\\/oogasawa");
        Matcher m = pDir.matcher(line);
        String result = m.replaceAll(System.getenv("HOME"));
        return result;
    }

}
