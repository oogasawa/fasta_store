package jp.ac.nig.ddbj.fastastore;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.TreeMap;

/** Information on the datasets that the BLAST Web application provided by DDBJ targets for search.
 *
 * <p>The information consists of two pairs of elements: the data set and the FASTA file set that make up the data set.</p>
 *
 * <h3>Data set</h3>
 *
 * <p>Specifically, the dataset for DDBJ BLAST Web application is as follows.</p>
 *
 * <pre>{@code
 *  "DDBJ_standard"
 *  "DDBJ_other",
 *  "rRNA16S",
 *  "RefSeq_daily",
 *  "RefSeq_genome",
 *  "RefSeq_RNA",
 *  "RefSeq_model",
 *  "DAD",
 *  "PatentAA",
 *  "UniProt"    
 * }</pre>
 *
 * <p>
 * The dataset is represented as the {@link WebBlastFastaDataset} class.
 * One BerkeleyDB database corresponds to each dataset.
 * </p>
 *
 * <h3>FASTA file set</h3>
 * 
 * <p>
 * The FASTA file set is represented as the {@link WebBlastFastaFiles} class.
 * A data set consists of one or more FASTA file sets.
 * Therefore, the WebBlastFastDataset object and the WebBlastFastFastaFiles object have a one-to-many relationship.
 * </p>
 *
 * この関係をハードコーディングしないで済ませる方法は、JSONを読み込むように作っとくことか ...
 */

public class WebBlastFastaInfo {

    // TreeMap<WebBlastFastaDataset, ArrayList<WebBlastFastaFiles> > info = new TreeMap<>(); 

    
    // /** Base Directory of the FASTA files.
    //  *
    //  * <p>Example:</p>
    //  *
    //  * <ul>
    //  * <li>{@code /home/w3wabi/BLAST}</li>
    //  * <li>{@code /home/you/blast_test_dataset}</li>
    //  * </ul>
    //  */ 
    // Path fastaBaseDir;


    // Path bdbEnvDir;

    
    // String[] datasets = {
    //     "DDBJ_standard",
    //     "DDBJ_other",
    //     "rRNA16S",
    //     "RefSeq_daily",
    //     "RefSeq_genome",
    //     "RefSeq_RNA",
    //     "RefSeq_model",
    //     "DAD",
    //     "PatentAA",
    //     "UniProt"    
    // };


    
    // /** Default constructor. */
    // public WebBlastFastaInfo() {

    //     // DDBJ_standard
    //     WebBlastFastaDataset dsObj = new WebBlastFastaDataset();
    //     dsObj.setDatasetName("DDBJ_standard");
    //     dsObj.setBdbEnvDir(this.bdbEnvDir);

        
        
    // }


    

    

    // /** Data structure representing the location of the FASTA files.
    //  * 
    //  * @param dsName Data set name.
    //  * @param fastaDir A subdirectory (under the {@code baseDir}) that contains the FASTA files.
    //  * @param extension FASTA file extension.
    //  */
    // public record FastaInfo(String dsName, Path fastaDir, String extension) {}

    // ArrayList<FastaInfo> fastaDirs = new ArrayList<>();

    
    // /** An array of taxa, under the {@code na/refseq/fasta/release} directory. */
    // String[] refseqTaxa = {
    //     "archaea", "bacteria",
    //     "fungi", "invertebrate",
    //     "mitochondrion", "plant",
    //     "plasmid", "plastid",
    //     "protozoa", "vertebrate_mammalian",
    //     "vertebrate_other", "virus", "other"
    // };

    // /** An array of RefSeq model species. */
    // String[] refseqModels = {
    //     "B_taurus",
    //     "D_rerio",
    //     "H_sapiens",
    //     "M_musculus",
    //     "R_norvegicus",
    //     "S_scrofa",
    //     "X_tropicalis"
    // };

    // // /** Default constructor. */
    // // public WebBlastFastaInfo() {

    // //     fastaDirs.add(new FastaInfo("DDBJ", Path.of("na/ddbj/fasta"), ".seq.gz"));
    // //     fastaDirs.add(new FastaInfo("rRNA16S", Path.of("na/16S/fasta"), ".fasta.gz"));
    // //     fastaDirs.add(new FastaInfo("RefsSeq_daily", Path.of("na/refseq/fasta/daily"), ".fna.gz"));

    // //     // for (String taxon: refseqTaxa) {
    // //     //     fastaDirs.add(new FastaInfo("RefsSeq_genomic", Path.of("na/refseq/fasta/release/" + taxon), ".genomic.fna.gz"));
    // //     // }
        
    // //     // for (String taxon: refseqTaxa) {
    // //     //     fastaDirs.add(new FastaInfo("RefsSeq_RNA", Path.of("na/refseq/fasta/release/" + taxon), ".rna.fna.gz"));
    // //     // }

    // //     // for (String model: refseqModels) {
    // //     //     fastaDirs.add(new FastaInfo("RefsSeq_Model", Path.of("na/refseq/fasta/" + model + "/mRNA_Prot"), ".fna.gz"));
    // //     // }

    // //     // fastaDirs.add(new FastaInfo("DAD", Path.of("aa/dad/fasta"), "fasta.gz"));
    // //     // fastaDirs.add(new FastaInfo("PatentAA", Path.of("aa/patent/fasta"), "gz"));
    // //     fastaDirs.add(new FastaInfo("UniProt", Path.of("aa/uniprot/fasta"), "fasta.gz"));
    // // }


    // public Path getFastaBaseDir() {
    //     return this.fastaBaseDir;
    // }


    // /** Returns an ArrayList object represeinting the location of the FASTA files. */
    // public ArrayList<FastaInfo> getFastaDirs() {
    //     return this.fastaDirs;
    // }
    
    // public void setFastaBaseDir(Path dir) {
    //     this.fastaBaseDir = dir;
    // }

    
    
}