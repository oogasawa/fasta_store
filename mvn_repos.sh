#!/bin/bash


function mvn_install() {
    cd /tmp
    git clone --branch $2 --depth 1 https://github.com/oogasawa/$1 
    cd $1   
    mvn clean compile install
    cd ..
    rm -Rf $1
}

mvn_install POJO-bdd       v1.4.2
mvn_install Utility-cli    v1.0.0
mvn_install Utility-files  v0.9.0
mvn_install Utility-types  v0.11.0
mvn_install Pipe           v1.2.0
mvn_install DataCell       v1.2.3
mvn_install POJO-actor     v1.2.0
mvn_install autonomous_blastdb_updator v0.3.0


