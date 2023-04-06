package jp.ac.nig.ddbj.fastastore;

import java.nio.file.Path;
import java.util.ArrayList;


public class WebBlastFastaDirs {

    /** Base Directory of the FASTA files.
     *
     * <p>Example:</p>
     *
     * <ul>
     * <li>{@code /home/w3wabi/BLAST}</li>
     * <li>{@code /home/you/blast_test_dataset}</li>
     * </ul>
     */ 
    Path fastaBaseDir;

    /** Data structure representing the location of the FASTA files.
     * 
     * @param dsName Data set name.
     * @param fastaDir A subdirectory (under the {@code baseDir}) that contains the FASTA files.
     * @param extension FASTA file extension.
     */
    public record FastaInfo(String dsName, Path fastaDir, String extension) {}

    ArrayList<FastaInfo> fastaDirs = new ArrayList<>();

    /** An array of taxa, under the {@code na/refseq/fasta/release} directory. */
    String[] refseqTaxa = {
        "archaea", "bacteria",
        "fungi", "invertebrate",
        "mitochondrion", "plant",
        "plasmid", "plastid",
        "protozoa", "vertebrate_mammalian",
        "vertebrate_other", "virus", "other"
    };

    /** An array of RefSeq model species. */
    String[] refseqModels = {
        "B_taurus",
        "D_rerio",
        "H_sapiens",
        "M_musculus",
        "R_norvegicus",
        "S_scrofa",
        "X_tropicalis"
    };

    /** Default constructor. */
    public WebBlastFastaDirs() {

        fastaDirs.add(new FastaInfo("DDBJ", Path.of("na/ddbj/fasta"), ".seq.gz"));
        fastaDirs.add(new FastaInfo("rRNA16S", Path.of("na/16S/fasta"), ".fasta.gz"));
        fastaDirs.add(new FastaInfo("RefsSeq_daily", Path.of("na/refseq/fasta/daily"), ".fna.gz"));

        for (String taxon: refseqTaxa) {
            fastaDirs.add(new FastaInfo("RefsSeq_genomic", Path.of("na/refseq/fasta/release/" + taxon), ".genomic.fna.gz"));
        }
        
        for (String taxon: refseqTaxa) {
            fastaDirs.add(new FastaInfo("RefsSeq_RNA", Path.of("na/refseq/fasta/release/" + taxon), ".rna.fna.gz"));
        }

        for (String model: refseqModels) {
            fastaDirs.add(new FastaInfo("RefsSeq_Model", Path.of("na/refseq/fasta/" + model + "/mRNA_Prot"), ".fna.gz"));
        }

        fastaDirs.add(new FastaInfo("DAD", Path.of("aa/dad/fasta"), "fasta.gz"));
        fastaDirs.add(new FastaInfo("PatentAA", Path.of("aa/patent/fasta"), "gz"));
        fastaDirs.add(new FastaInfo("UniProt", Path.of("aa/uniprot/fasta"), "fasta.gz"));
    }


    public Path getFastaBaseDir() {
        return this.fastaBaseDir;
    }


    /** Returns an ArrayList object represeinting the location of the FASTA files. */
    public ArrayList<FastaInfo> getFastaDirs() {
        return this.fastaDirs;
    }
    
    public void setFastaBaseDir(Path dir) {
        this.fastaBaseDir = dir;
    }

    
    
}
