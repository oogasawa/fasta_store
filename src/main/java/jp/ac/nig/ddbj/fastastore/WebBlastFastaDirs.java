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
     * <li>{@code /home/you/blast_test_data}</li>
     * </ul>
     */ 
    Path baseDir;

    /** Data structure representing the location of the FASTA files.
     * 
     * @param dsName Data set name.
     * @param fastaDir A subdirectory (under the {@code baseDir}) that contains the FASTA files.
     * @param extension FASTA file extension.
     */
    public record FastaDir(String dsName, Path fastaDir, String extension) {}

    ArrayList<FastaDir> fastaDirs = new ArrayList<>();

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
        fastaDirs.add(new FastaDir("DDBJ", Path.of("na/ddbj/fasta"), ".seq.gz"));
        fastaDirs.add(new FastaDir("rRNA16S", Path.of("na/16S/fasta"), ".fasta.gz"));
        fastaDirs.add(new FastaDir("RefsSeq_daily", Path.of("na/refseq/fasta/daily"), ".fna.gz"));

        for (String taxon: refseqTaxa) {
            fastaDirs.add(new FastaDir("RefsSeq_genomic", Path.of("na/refseq/fasta/release/" + taxon), ".genomic.fna.gz"));
        }
        
        for (String taxon: refseqTaxa) {
            fastaDirs.add(new FastaDir("RefsSeq_RNA", Path.of("na/refseq/fasta/release/" + taxon), ".rna.fna.gz"));
        }

        for (String model: refseqModels) {
            fastaDirs.add(new FastaDir("RefsSeq_Model", Path.of("na/refseq/fasta/" + model + "/mRNA_Prot"), ".fna.gz"));
        }

        fastaDirs.add(new FastaDir("DAD", Path.of("aa/dad/fasta"), "fasta.gz"));
        fastaDirs.add(new FastaDir("PatentAA", Path.of("aa/patent/fasta"), "gz"));
        fastaDirs.add(new FastaDir("UniProt", Path.of("aa/uniprot/fasta"), "fasta.gz"));
    }


    public Path getBaseDir() {
        return this.baseDir;
    }


    /** Returns an ArrayList object represeinting the location of the FASTA files. */
    public ArrayList<FastaDir> getFastaDirs() {
        return this.fastaDirs;
    }
    
    public void setBaseDir(Path dir) {
        this.baseDir = dir;
    }

    
    
}
