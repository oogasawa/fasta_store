#!/bin/bash

export LANG=en_US.UTF-8
mvn javadoc:javadoc
rm -Rf ~/public_html/javadoc/fasta_store
mv target/site ~/public_html/javadoc/fasta_store
