#!/bin/bash

#$ -cwd 
#$ -V 
#$ -l epyc
#$ -l s_vmem=40G 
#$ -l mem_req=40G
#$ -N fastastore
#$ -S /bin/bash

java -Xmx 32G -cp target/fastastore-fat.jar jp.ac.nig.ddbj.fastastore.FastaStorePut \
    $HOME/tmp/fastastore \
    $HOME/blast_test_dataset/na/refseq/fasta/daily \
    "fna.gz"
