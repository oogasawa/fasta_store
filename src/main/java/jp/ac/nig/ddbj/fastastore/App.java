package jp.ac.nig.ddbj.fastastore;


import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import jp.ac.nig.ddbj.fastastore.team.WebBlastFastaTeam;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.regex.Pattern;

import com.github.oogasawa.utility.cli.CliCommands;


public class App
{
    public static void main( String[] args )
    {

        var helpStr = "java -jar fastastore-fat.jar <command> <options>";
        var cli = new CliCommands();

        cli.addCommand("fasta:put", fastaPutOptions(),
                       "Read all FASTA files in a given directory to a BerkeleyDB database.");
        cli.addCommand("fasta:get", fastaGetOptions(),
                       "Search a FASTA entry corresponding to a given sequence ID in a BerkeleyDB database.");
        cli.addCommand("fasta:export", fastaExportOptions(),
                       "Export all FASTA entries in a given BerkeleyDB database.");
        cli.addCommand("webblast:putAll", webBlastPutAllOptions(),
                       "Create a series of databases consisting of all FASTA entries used in DDBJ's BLAST Web application.");

        

        try {

            CommandLine cmd = cli.parse(args);

            if (cli.getCommand() == null) {
                // check universal options.
                if (cmd.hasOption("h") || cmd.hasOption("help")) {
                    cli.printHelp(helpStr);
                }

            }
            else if (cli.getCommand().equals("fasta:put")) {
                FastaStorePut store = new FastaStorePut();
                //store.setEnvDir(Path.of(cmd.getOptionValue("dbDir")));
                
                
                store.readAll(Path.of(cmd.getOptionValue("fastaDir")),
                              Pattern.compile(cmd.getOptionValue("extension")),
                              Path.of(cmd.getOptionValue("dbDir")),
                              cmd.getOptionValue("dataset") );
            }
            else if (cli.getCommand().equals("fasta:get")) {
                FastaStoreGet store = new FastaStoreGet();
                store.setEnvDir(Path.of(cmd.getOptionValue("dbDir")));
                store.search(cmd.getOptionValue("seqID"));
            }
            else if (cli.getCommand().equals("fasta:export")) {
                FastaExporter store = new FastaExporter();
                store.setEnvDir(Path.of(cmd.getOptionValue("dbDir")));
                store.export(cmd.getOptionValue("outfile"));
            }
            else if (cli.getCommand().equals("webblast:putAll")) {
                
                WebBlastFastaTeam team = new WebBlastFastaTeam();
                
                // team.setBdbDir(cmd.getOptionValue("dbDir"));
                // team.start();
            }
            else {
                cli.printHelp(helpStr);
            }

        } catch (ParseException e) {
            System.err.println("Parsing failed.  Reason: " + e.getMessage());
            cli.printHelp(helpStr);
        }
    }



    public static Options fastaPutOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("dbDir")
                        .option("d")
                        .longOpt("dbDir")
                        .hasArg(true)
                        .argName("dbDir")
                        .desc("An Environment (directory) of BDB. (e.g. $HOME/tmp/fastastore)")
                        .required(true)
                        .build());

        opts.addOption(Option.builder("fastaDir")
                        .option("f")
                        .longOpt("fastaDir")
                        .hasArg(true)
                        .argName("fastaDir")
                        .desc("A directory where FASTA files are located. (e.g. $HOME/BLAST/na/refseq/fasta/daily)")
                        .required(true)
                        .build());

        opts.addOption(Option.builder("filePattern")
                        .option("p")
                        .longOpt("filePattern")
                        .hasArg(true)
                        .argName("filePattern")
                        .desc("File pattern string of FASTA files. (e.g. \"\\\\.fna\\\\.gz\")")
                        .required(true)
                        .build());

        opts.addOption(Option.builder("dataset")
                        .option("s")
                        .longOpt("dataset")
                        .hasArg(true)
                        .argName("dataset")
                        .desc("dataset Name (e.g. DDBJ_standard, DDBJ_other, ...)")
                        .required(true)
                        .build());

        
        return opts;
    }


    public static Options fastaGetOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("dbDir")
                        .option("d")
                        .longOpt("dbDir")
                        .hasArg(true)
                        .argName("dbDir")
                        .desc("An Environment (directory) of BDB. (e.g. $HOME/tmp/fastastore)")
                        .required(true)
                        .build());

        opts.addOption(Option.builder("seqID")
                        .option("s")
                        .longOpt("seqID")
                        .hasArg(true)
                        .argName("seqID")
                        .desc("A sequence ID to be searched.")
                        .required(true)
                        .build());


        
        return opts;
    }



    public static Options fastaExportOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("dbDir")
                        .option("d")
                        .longOpt("dbDir")
                        .hasArg(true)
                        .argName("dbDir")
                        .desc("An Environment (directory) of BDB. (e.g. $HOME/tmp/fastastore)")
                        .required(true)
                        .build());

        opts.addOption(Option.builder("outfile")
                        .option("o")
                        .longOpt("outfile")
                        .hasArg(true)
                        .argName("outfile")
                        .desc("A destination file for FASTA data. (e.g. result.fasta)")
                        .required(true)
                        .build());


        
        return opts;
    }

    
    public static Options webBlastPutAllOptions() {
        Options opts = new Options();

        opts.addOption(Option.builder("dbDir")
                        .option("d")
                        .longOpt("dbDir")
                        .hasArg(true)
                        .argName("dbDir")
                        .desc("An Environment (directory) of BDB. (e.g. $HOME/tmp/fastastore)")
                        .required(true)
                        .build());

        return opts;
    }


    
}
