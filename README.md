# fasta_store

## 概要

FASTA ファイルのデータベースの作成と検索を行う。

- DDBJ Web BLAST の検索対象となっている FASTA ファイルに関するデータベースを作成する。
- sequence ID を与えると、対応する塩基配列またはアミノ酸配列を FASTA フォーマットで返す。


## 動作環境

- JDK20 以上でコンパイル、実行すること。

## コンパイル

まず、本リポジトリをクローンする。

``` 
git clone https://github.com/oogasawa/fasta_store
```

このリポジトリが依存するいくつかの Java ライブラリは Maven Repository に登録されていないので、
以下のスクリプト(`mvn_repos.sh`)により開発環境の`$HOME/.m2/repository`以下にインストールする。

``` 
cd fasta_store
bash mvn_repos.sh
```

以下のようにしてコンパイルし、fat-jar を作る。

```
mvn clean compile package assembly:single
```

- 本プログラムは JDK20 の preview 機能(virtual thread)を使っているので、`mvn clean`をいつも指定しておくことにより`--enable-preview`オプションに関係するコンパイルエラー発生の予防となる。


これにより`target/fasta-store-VERSION.jar`という jar ファイルと`target/fastastore-fat.jar`という fat-jar ファイルができる。


## データベースの作成

どのディレクトリのどのファイルを見に行っているかは`src/main/resoureces/fasta_na.yaml`および`src/main/resoureces/fasta_na.yaml`に書いてある。

FASTA ファイルの置き場がこの設定ファイルに書いてあるとおりであるとすると、以下のようにプログラムを呼び出すことで FASTA ファイルのデータベースが作成される。

```
java -jar target/fastastore-fat.jar webblast:putAll -d <your_database_dir>
```

## データベースの検索

sequence ID を与えると、対応する塩基配列またはアミノ酸配列を FASTA フォーマットで返す。

```
java -jar target/fastastore-fat.jar webblast:getEntry -q <sequenceID> -d <your_database_dir>
```

sequence ID は複数指定可能である。
（データベースに塩基配列とアミノ酸配列が混在している場合には、塩基配列の ID とアミノ酸配列の ID が混在していても構わない。）

例: 

```
java -jar target/fastastore-fat.jar webblast:getEntry -q "id,id,id" -d $HOME/tmp/fastastore
```



## ヘルプの表示

引数なしで呼び出すとヘルプが表示される。

```
$ java -jar target/fastastore-fat.jar 
Parsing failed.  Reason: ERROR: No arguments.

java -jar fastastore-fat.jar <command> <options>

The following is the usage of each command.

Export all FASTA entries in a given BerkeleyDB database.
usage: fasta:export -d <dbDir> -o <outfile>
 -d,--dbDir <dbDir>       An Environment (directory) of BDB. (e.g.
                          $HOME/tmp/fastastore)
 -o,--outfile <outfile>   A destination file for FASTA data. (e.g.
                          result.fasta)

Search a FASTA entry corresponding to a given sequence ID in a BerkeleyDB database.
usage: fasta:get -d <dbDir> -s <seqID>
 -d,--dbDir <dbDir>   An Environment (directory) of BDB. (e.g.
                      $HOME/tmp/fastastore)
 -s,--seqID <seqID>   A sequence ID to be searched.

Read all FASTA files in a given directory to a BerkeleyDB database.
usage: fasta:put -d <dbDir> -f <fastaDir> -p <filePattern> -s <dataset>
 -d,--dbDir <dbDir>               An Environment (directory) of BDB. (e.g.
                                  $HOME/tmp/fastastore)
 -f,--fastaDir <fastaDir>         A directory where FASTA files are
                                  located. (e.g.
                                  $HOME/BLAST/na/refseq/fasta/daily)
 -p,--filePattern <filePattern>   File pattern string of FASTA files.
                                  (e.g. "\\.fna\\.gz")
 -s,--dataset <dataset>           dataset Name (e.g. DDBJ_standard,
                                  DDBJ_other, ...)

Create a series of databases consisting of all FASTA entries used in DDBJ's BLAST Web application.
usage: webblast:putAll -d <dbDir>
 -d,--dbDir <dbDir>   An Environment (directory) of BDB. (e.g.
                      $HOME/tmp/fastastore)

```

