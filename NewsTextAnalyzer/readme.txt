Had to install Virtuoso local libraries through an in-project local repository on lib folder via the following commands:
mvn install:install-file -Dfile=virt_jena3.jar -DgroupId=com.openlink.virtuoso -DartifactId=virt_jena3 -Dversion=3.0 -Dpackaging=jar -DlocalRepositoryPath=../lib
mvn install:install-file -Dfile=virtjdbc4.jar -DgroupId=com.openlink.virtuoso -DartifactId=virtjdbc4 -Dversion=4.0 -Dpackaging=jar -DlocalRepositoryPath=../lib
