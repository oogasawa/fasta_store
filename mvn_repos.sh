#!/bin/bash


function mvn_install() {
    cd ..
    git clone https://github.com/oogasawa/$1 
    cd $1   
    mvn clean compile install
}

mvn_install POJO-bdd
mvn_install Utility-cli
mvn_install Utility-files
mvn_install Utility-types
mvn_install Pipe
mvn_install DataCell
mvn_install POJO-actor
mvn_install autonomous_blastdb_updator


