# Mining pool signalling

## Overview
This tool aims at helping mining pool operators to prove their fairiness toward the users of the pool. 
Implementation details and tech specs can be found in _allegato1_ . 

## Overview
This tool is designed as a microservice, docker images can be built via the provided Dockerfile. 
You can create trees by PUTting a list of "Accounts" via the API, accounts are comprised of {user, balance, nonce} 
with balance being a floating point number. Is also possible to retrieve a Proof for a user given its account 
and the stored tree's root-digest, likewise you can ask the API to verify a proof for a given account.

## Development

The application is written in Scala and requires JDK8 and sbt. The data is stored both in memory and on disk,
the disk storage is in plain text (JSON format). 


## Installation

The packaging is done via <https://www.scala-sbt.org/sbt-native-packager>, you can produce a self contained executable (packed in a tarball)
by issuing: `sbt universal:packageZipTarball` , this will create a .tar.gz inside "target/universal" folder, to run the 
application just use the launchers in 'bin' folder.Custom parameters can be provided via cmd line as in `-Dapi.port=8088`, a complete list can be found in application.conf file.