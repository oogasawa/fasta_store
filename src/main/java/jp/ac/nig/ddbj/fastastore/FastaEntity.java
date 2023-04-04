package jp.ac.nig.ddbj.fastastore;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;


/** An entity class of BerkeleyDB DPL that holds a single FASTA entry. 
 *
 * A FASTA entry consists of two parts:
 * the <b>definition line</b> and <b>sequence lines</b>.
 *
 * The definition line is a single line
 * that begins with the mandatory {@code >} symbol
 * immediately followed by an <b>identifier</b> and then a <b>description</b>.
 *
 * There are no spaces between {@code >} and the identifier.
 * The identifier itself must not contain any whitespace.
 *
 * The description is free-form text that may contain
 * any (ASCII) characters.
 *
 * @author <a href="mailto:oogasawa@nig.ac.jp">Osmau Ogasawara</a>
 */
@Entity
public class FastaEntity {


    Pattern pSeqId = Pattern.compile("^>(\\S+?)\\|");
    
    @PrimaryKey
    private String sequenceId = null;

    /** A field to hold a single FASTA entry.
     *
     * A FASTA entry can be too large to fit in a String type object.
     * To avoid this limitation, this field holds a single line of FASTA data as one element of an ArrayList.
     * 
     */
    private ArrayList<String> fastaData = new ArrayList<String>();


    /**  Default constructor. */
    public FastaEntity() {  }

    
    /** Adds a line of a FASTA entry for the value of the key-value pair.
     *
     * @param line A line of a FASTA entry.
     */
    public void addLine(String line) {
        fastaData.add(line);
    }

    /** Returns the definition line of the FASTA entry.
     *
     * @return the definition line. (if the FASTA entry is empty, return {@code null}.)
     */
    public String getDefinitionLine() {
        if (fastaData.size() > 0) {
            return fastaData.get(0);
        }
        else {
            return null;
        }
    }

    
    public ArrayList<String> getfastaData() {
        return fastaData;
    }

    
    public String getSequenceId() {
        return sequenceId;
    }


    /** Parses a definition line and returns a sequenceId.
     *
     * The FASTA definition line is assumed to have the string pattern represented by the following example.
     *
     * <pre>{@code
     * >CP100557|CP100557.1 Gallus gallus breed Huxu chromosome 3
     * }</pre>
     *
     * In this example, the sequence ID is {@code CP100557}.
     */
    public String parseSequenceId(String definitionLine) {
        String seqId = null;
        
        Matcher m = pSeqId.matcher(definitionLine);
        if (m.find()) {
            seqId = m.group(1);
        }
        
        return seqId;
    }
    
    public void setFastaData(ArrayList<String> value) {
        this.fastaData = value;
    }

    
    public void setSequenceId(String id) {
        this.sequenceId = id;
    }


}




